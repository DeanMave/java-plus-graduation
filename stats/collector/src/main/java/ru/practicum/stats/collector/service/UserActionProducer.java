package ru.practicum.stats.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.stats.avro.UserActionAvro;
import ru.practicum.stats.proto.UserActionProto;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionProducer implements UserActionHandler {

    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;
    private final UserActionMapper mapper;

    @Value("${kafka.topic.user-action}")
    private String topic;

    @Override
    public void handle(UserActionProto proto) {
        if (proto == null) {
            log.warn("Получено пустое событие, пропускаю");
            return;
        }

        UserActionAvro avro = mapper.toAvro(proto);

        kafkaTemplate.send(topic, avro)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Ошибка отправки события в {}", topic, ex);
                        return;
                    }
                    var md = result.getRecordMetadata();
                    log.debug("Отправлено: topic={}, partition={}, offset={}",
                            md.topic(), md.partition(), md.offset());
                });

        log.debug("Событие отправлено: userId={}, eventId={}, action={}",
                proto.getUserId(), proto.getEventId(), proto.getActionType());
    }
}
