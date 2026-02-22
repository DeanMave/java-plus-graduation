package ru.practicum.aggregator.service;

import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.avro.UserActionAvro;

import java.util.List;

public interface EventsSimilarityService {
    List<EventSimilarityAvro> countSimilarity(UserActionAvro userAction);
}
