package ru.practicum.analyzer.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.mapper.RecommendationsMapper;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.model.UserAction;
import ru.practicum.analyzer.repository.SimilarityRepository;
import ru.practicum.analyzer.repository.UserInteractionRepository;
import ru.practicum.stats.proto.InteractionsCountRequestProto;
import ru.practicum.stats.proto.RecommendedEventProto;
import ru.practicum.stats.proto.SimilarEventsRequestProto;
import ru.practicum.stats.proto.UserPredictionsRequestProto;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class RecommendationsServiceImpl implements RecommendationsService {

    private final UserInteractionRepository userInteractionRepository;
    private final SimilarityRepository similarityRepository;
    private final RecommendationsMapper mapper;

    private static final int K_NEIGHBORS = 10;

    @Override
    public Stream<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        log.info("Получение рекомендаций для пользователя {} (max_results: {})",
                request.getUserId(), request.getMaxResults());

        Long userId = request.getUserId();
        int maxResults = (int) request.getMaxResults();

        if (maxResults <= 0) {
            log.warn("Запрошено недопустимое количество результатов: {}", maxResults);
            return Stream.empty();
        }

        List<UserAction> userInteractions = userInteractionRepository.findAllByUserId(userId);

        if (userInteractions.isEmpty()) {
            log.info("Пользователь {} не имеет взаимодействий, рекомендации невозможны", userId);
            return Stream.empty();
        }

        List<UserAction> sortedInteractions = mapper.sortByTimestampDesc(userInteractions);

        Set<Long> alreadyInteractedEventIds = sortedInteractions.stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        Map<Long, Double> candidateEvents = new HashMap<>();

        for (UserAction interaction : sortedInteractions) {
            Long eventId = interaction.getEventId();
            List<EventSimilarity> similarities = similarityRepository.findAllByEventId(eventId);

            for (EventSimilarity similarity : similarities) {
                Long candidateEventId = similarity.getEvent1().equals(eventId)
                        ? similarity.getEvent2()
                        : similarity.getEvent1();

                if (alreadyInteractedEventIds.contains(candidateEventId)) {
                    continue;
                }

                double currentScore = candidateEvents.getOrDefault(candidateEventId, 0.0);
                candidateEvents.put(candidateEventId, Math.max(currentScore, similarity.getSimilarity()));
            }
        }

        if (candidateEvents.isEmpty()) {
            log.info("Для пользователя {} не найдено кандидатов для рекомендаций", userId);
            return Stream.empty();
        }

        return candidateEvents.entrySet().stream()
                .parallel()
                .map(entry -> {
                    Long candidateEventId = entry.getKey();
                    Double predictedScore = calculatePredictedScore(userId, candidateEventId, alreadyInteractedEventIds);

                    if (predictedScore != null) {
                        return mapper.toRecommendedEventProto(candidateEventId, predictedScore);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .limit(maxResults);
    }


    @Override
    public Stream<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        log.info("Поиск похожих событий для события {} (user_id: {}, max_results: {})",
                request.getEventId(), request.getUserId(), request.getMaxResults());

        Long eventId = request.getEventId();
        Long userId = request.getUserId();
        int maxResults = (int) request.getMaxResults();

        if (maxResults <= 0) {
            log.warn("Запрошено недопустимое количество результатов: {}", maxResults);
            return Stream.empty();
        }

        List<EventSimilarity> similarities = similarityRepository.findAllByEventId(eventId);

        if (similarities.isEmpty()) {
            log.info("Для события {} не найдено похожих событий", eventId);
            return Stream.empty();
        }

        if (userId == 0) {
            return similarities.stream()
                    .sorted(Comparator.comparing(EventSimilarity::getSimilarity).reversed())
                    .limit(maxResults)
                    .map(similarity -> mapper.toRecommendedEventProto(similarity, eventId))
                    .filter(Objects::nonNull);
        }

        Set<Long> userInteractedEventIds = userInteractionRepository.findAllByUserId(userId).stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        return similarities.stream()
                .filter(similarity -> {
                    Long otherEventId = similarity.getEvent1().equals(eventId)
                            ? similarity.getEvent2()
                            : similarity.getEvent1();
                    return !userInteractedEventIds.contains(otherEventId);
                })
                .sorted(Comparator.comparing(EventSimilarity::getSimilarity).reversed())
                .limit(maxResults)
                .map(similarity -> mapper.toRecommendedEventProto(similarity, eventId))
                .filter(Objects::nonNull);
    }

    @Override
    public Stream<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        log.info("Получение суммы взаимодействий для {} событий", request.getEventIdCount());

        return request.getEventIdList().stream()
                .distinct()
                .map(eventId -> {
                    List<UserAction> interactions = userInteractionRepository.findAllByEventId(eventId);

                    if (interactions.isEmpty()) {
                        return mapper.toRecommendedEventProto(eventId, 0.0);
                    }

                    double totalScore = interactions.stream()
                            .mapToDouble(UserAction::getRating)
                            .sum();

                    return mapper.toRecommendedEventProto(eventId, totalScore);
                })
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed());
    }

    private Double calculatePredictedScore(Long userId, Long candidateEventId, Set<Long> userInteractedEventIds) {
        List<EventSimilarity> allSimilarities = similarityRepository.findAllByEventId(candidateEventId);

        List<EventSimilarity> neighborSimilarities = allSimilarities.stream()
                .filter(similarity -> {
                    Long similarEventId = similarity.getEvent1().equals(candidateEventId)
                            ? similarity.getEvent2()
                            : similarity.getEvent1();
                    return userInteractedEventIds.contains(similarEventId);
                })
                .sorted(Comparator.comparing(EventSimilarity::getSimilarity).reversed())
                .limit(K_NEIGHBORS)
                .toList();

        if (neighborSimilarities.isEmpty()) {
            return null;
        }

        double weightedSum = 0.0;
        double similaritySum = 0.0;

        for (EventSimilarity similarity : neighborSimilarities) {
            Long similarEventId = similarity.getEvent1().equals(candidateEventId)
                    ? similarity.getEvent2()
                    : similarity.getEvent1();

            Optional<UserAction> userAction = userInteractionRepository.findByUserIdAndEventId(userId, similarEventId);

            if (userAction.isPresent()) {
                double rating = userAction.get().getRating();
                double similarityScore = similarity.getSimilarity();

                weightedSum += rating * similarityScore;
                similaritySum += similarityScore;
            }
        }

        if (similaritySum == 0) {
            return 0.0;
        }

        return weightedSum / similaritySum;
    }
}
