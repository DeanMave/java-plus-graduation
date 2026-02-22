package ru.practicum.stats.avro.deserializer;

import ru.practicum.stats.avro.EventSimilarityAvro;

public class EventsSimilarityDeserializer extends BaseAvroDeserializer<EventSimilarityAvro> {
    public EventsSimilarityDeserializer() {
        super(EventSimilarityAvro.getClassSchema());
    }
}
