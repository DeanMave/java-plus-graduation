package ru.practicum.analyzer.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.stats.avro.EventSimilarityAvro;

import java.time.LocalDateTime;
import java.time.ZoneId;


@Mapper(componentModel = "spring", imports = {LocalDateTime.class, ZoneId.class, Math.class})
public interface EventSimilarityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "similarity", source = "score")
    @Mapping(target = "event1", expression = "java(Math.min(avro.getEventA(), avro.getEventB()))")
    @Mapping(target = "event2", expression = "java(Math.max(avro.getEventA(), avro.getEventB()))")
    @Mapping(target = "timestamp", expression = "java(LocalDateTime.ofInstant(avro.getTimestamp(), ZoneId.systemDefault()))")
    EventSimilarity toEntity(EventSimilarityAvro avro);
}