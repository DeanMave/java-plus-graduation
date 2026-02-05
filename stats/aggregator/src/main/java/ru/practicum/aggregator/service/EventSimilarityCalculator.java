package ru.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.aggregator.config.SimilarityConfig;
import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSimilarityCalculator implements EventsSimilarityService {

    private final SimilarityConfig config;

    private final Map<Long, Map<Long, Double>> userMaxWeightsByEvent = new ConcurrentHashMap<>();
    private final Map<Long, Double> totalWeightsByEvent = new ConcurrentHashMap<>();
    private final Map<Long, Map<Long, Double>> minWeightsByPair = new ConcurrentHashMap<>();

    @Override
    public List<EventSimilarityAvro> countSimilarity(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
        double weight = config.getActionWeights().get(action.getActionType().name());
        Instant timestamp = action.getTimestamp();

        Double previous = userMaxWeightsByEvent
                .computeIfAbsent(eventId, k -> new ConcurrentHashMap<>())
                .get(userId);

        if (previous != null && weight <= previous) {
            log.debug("Вес не увеличился: user={}, event={}", userId, eventId);
            return List.of();
        }

        double oldWeight = previous == null ? 0.0 : previous;
        userMaxWeightsByEvent.get(eventId).put(userId, weight);

        updateTotalWeight(eventId, oldWeight, weight);

        return recalcSimilarities(eventId, userId, oldWeight, weight, timestamp);
    }

    private void updateTotalWeight(long eventId, double oldWeight, double newWeight) {
        totalWeightsByEvent.merge(eventId, newWeight - oldWeight, Double::sum);
    }

    private List<EventSimilarityAvro> recalcSimilarities(
            long updatedEvent,
            long userId,
            double oldWeight,
            double newWeight,
            Instant timestamp
    ) {
        List<EventSimilarityAvro> result = new ArrayList<>();

        for (var entry : userMaxWeightsByEvent.entrySet()) {
            long otherEvent = entry.getKey();
            if (otherEvent == updatedEvent) continue;

            Double otherWeight = entry.getValue().get(userId);
            if (otherWeight == null) continue;

            double delta = calcMinDelta(oldWeight, newWeight, otherWeight);
            if (delta != 0) updateMinSum(updatedEvent, otherEvent, delta);

            double similarity = calcSimilarity(updatedEvent, otherEvent);
            if (similarity > 0) {
                result.add(buildSimilarity(updatedEvent, otherEvent, similarity, timestamp));
            }
        }

        return result;
    }

    private double calcMinDelta(double oldW, double newW, double otherW) {
        return Math.min(newW, otherW) - Math.min(oldW, otherW);
    }

    private void updateMinSum(long a, long b, double delta) {
        long first = Math.min(a, b);
        long second = Math.max(a, b);

        minWeightsByPair
                .computeIfAbsent(first, k -> new ConcurrentHashMap<>())
                .merge(second, delta, Double::sum);
    }

    private double calcSimilarity(long a, long b) {
        long first = Math.min(a, b);
        long second = Math.max(a, b);

        Double sMin = minWeightsByPair
                .computeIfAbsent(first, k -> new ConcurrentHashMap<>())
                .get(second);

        if (sMin == null || sMin == 0) return 0;

        Double sA = totalWeightsByEvent.get(a);
        Double sB = totalWeightsByEvent.get(b);

        if (sA == null || sB == null || sA == 0 || sB == 0) return 0;

        return sMin / (Math.sqrt(sA) * Math.sqrt(sB));
    }

    private EventSimilarityAvro buildSimilarity(long a, long b, double score, Instant ts) {
        long first = Math.min(a, b);
        long second = Math.max(a, b);

        return EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore(score)
                .setTimestamp(ts)
                .build();
    }
}
