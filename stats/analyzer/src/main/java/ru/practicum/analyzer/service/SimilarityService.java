package ru.practicum.analyzer.service;

import ru.practicum.stats.avro.EventSimilarityAvro;

public interface SimilarityService {
    void save(EventSimilarityAvro eventSimilarityAvro);
}
