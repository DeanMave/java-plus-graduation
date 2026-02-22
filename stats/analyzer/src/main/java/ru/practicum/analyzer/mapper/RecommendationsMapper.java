package ru.practicum.analyzer.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.model.UserAction;
import ru.practicum.stats.proto.RecommendedEventProto;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class RecommendationsMapper {

    public RecommendedEventProto toRecommendedEventProto(EventSimilarity similarity, Long targetEventId) {
        if (similarity == null) return null;

        Long recommendedEventId = Objects.equals(similarity.getEvent1(), targetEventId)
                ? similarity.getEvent2()
                : similarity.getEvent1();

        return RecommendedEventProto.newBuilder()
                .setEventId(recommendedEventId)
                .setScore(similarity.getSimilarity())
                .build();
    }

    public RecommendedEventProto toRecommendedEventProto(Long eventId, Double score) {
        if (eventId == null || score == null) return null;

        return RecommendedEventProto.newBuilder()
                .setEventId(eventId)
                .setScore(score)
                .build();
    }

    public Map<Long, List<UserAction>> groupByUser(List<UserAction> actions) {
        return actions.stream()
                .collect(Collectors.groupingBy(UserAction::getUserId));
    }

    public List<UserAction> sortByTimestampDesc(List<UserAction> actions) {
        return actions.stream()
                .sorted(Comparator.comparing(UserAction::getTimestamp).reversed())
                .toList();
    }
}
