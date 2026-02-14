package ru.practicum.analyzer.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import ru.practicum.analyzer.service.UserActionService;
import ru.practicum.stats.avro.UserActionAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionListener {

    private final UserActionService userActionService;

    @KafkaListener(
            topics = "stats.user-actions.v1",
            containerFactory = "userActionFactory"
    )
    public void onMessage(
            UserActionAvro avro,
            Acknowledgment ack,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
    ) {
        log.trace("Получено действие: partition={}, offset={}", partition, offset);

        try {
            userActionService.save(avro);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Ошибка обработки UserAction", e);
        }
    }
}
