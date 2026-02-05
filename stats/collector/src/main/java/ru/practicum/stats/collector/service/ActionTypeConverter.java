package ru.practicum.stats.collector.service;

import ru.practicum.stats.avro.ActionTypeAvro;
import ru.practicum.stats.proto.ActionTypeProto;

public class ActionTypeConverter {

    public static ActionTypeAvro convert(ActionTypeProto proto) {
        if (proto == null) {
            return null;
        }

        return switch (proto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unknown ActionTypeProto: UNRECOGNIZED");
        };
    }
}