package ru.practicum.stats.collector.service;

import org.springframework.stereotype.Component;
import ru.practicum.stats.avro.ActionTypeAvro;
import ru.practicum.stats.avro.UserActionAvro;
import ru.practicum.stats.proto.ActionTypeProto;
import ru.practicum.stats.proto.UserActionProto;

import java.time.Instant;

@Component
public class UserActionMapper {

    public UserActionAvro toAvro(UserActionProto proto) {
        Instant ts = proto.hasTimestamp()
                ? Instant.ofEpochSecond(proto.getTimestamp().getSeconds(), proto.getTimestamp().getNanos())
                : Instant.now();

        return UserActionAvro.newBuilder()
                .setUserId(proto.getUserId())
                .setEventId(proto.getEventId())
                .setActionType(mapAction(proto.getActionType()))
                .setTimestamp(ts)
                .build();
    }

    private ActionTypeAvro mapAction(ActionTypeProto proto) {
        return switch (proto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> throw new IllegalArgumentException("Неизвестный тип действия: " + proto);
        };
    }
}
