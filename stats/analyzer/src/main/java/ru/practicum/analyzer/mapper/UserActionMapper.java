package ru.practicum.analyzer.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.analyzer.model.UserAction;
import ru.practicum.stats.avro.ActionTypeAvro;
import ru.practicum.stats.avro.UserActionAvro;

import java.time.LocalDateTime;
import java.time.ZoneId;


@Mapper(componentModel = "spring", imports = {LocalDateTime.class, ZoneId.class})
public interface UserActionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rating", expression = "java(mapRating(avro.getActionType()))")
    @Mapping(target = "timestamp", expression = "java(LocalDateTime.ofInstant(avro.getTimestamp(), ZoneId.systemDefault()))")
    UserAction toEntity(UserActionAvro avro);

    default Double mapRating(ActionTypeAvro type) {
        if (type == null) return null;
        return switch (type) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}