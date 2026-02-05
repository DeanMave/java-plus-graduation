package ru.practicum.stats.collector.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.practicum.stats.avro.UserActionAvro;
import ru.practicum.stats.proto.UserActionProto;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class UserActionHandlerImpl implements UserActionHandler {

    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;
    private final String topic;

    public UserActionHandlerImpl(KafkaTemplate<String, SpecificRecordBase> kafkaTemplate,
                                 @Value("${kafka.topic.user-action}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        log.info("UserActionHandlerImpl инициализирован с топиком: {}", topic);
    }

    @Override
    public void handle(UserActionProto event) {
        if (event == null) {
            log.error("Получено пустое событие, обработка прервана");
            return;
        }

        try {
            UserActionAvro avroEvent = mapToAvro(event);
            log.info("Начинаю отправку сообщения {} в топик {}", avroEvent, topic);

            CompletableFuture<SendResult<String, SpecificRecordBase>> future = kafkaTemplate.send(topic, avroEvent);

            future.whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error("Ошибка отправки сообщения в топик {}", topic, exception);
                } else {
                    log.info("Сообщение отправлено в топик {} partition {} offset {}",
                            topic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                }
            });

            log.info("Событие отправлено на обработку: userId={}, eventId={}, actionType={}",
                    event.getUserId(), event.getEventId(), event.getActionType());
        } catch (Exception e) {
            log.error("Ошибка обработки события", e);
        }
    }

    protected UserActionAvro mapToAvro(UserActionProto event) {
        if (event == null) {
            throw new IllegalArgumentException("Событие не может быть null");
        }

        Instant timestamp = event.hasTimestamp()
                ? convertTimestampToInstant(event.getTimestamp())
                : Instant.now();

        return UserActionAvro.newBuilder()
                .setUserId(event.getUserId())
                .setEventId(event.getEventId())
                .setActionType(ActionTypeConverter.convert(event.getActionType()))
                .setTimestamp(timestamp)
                .build();
    }

    private Instant convertTimestampToInstant(com.google.protobuf.Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}