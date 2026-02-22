package ru.practicum.analyzer.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import ru.practicum.analyzer.service.SimilarityService;
import ru.practicum.stats.avro.EventSimilarityAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimilarityListener {

    private final SimilarityService similarityService;

    @KafkaListener(
            topics = "stats.events-similarity.v1",
            containerFactory = "similarityFactory"
    )
    public void onMessage(
            EventSimilarityAvro avro,
            Acknowledgment ack,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
    ) {
        log.trace("Получено сходство: partition={}, offset={}", partition, offset);

        try {
            similarityService.save(avro);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Ошибка обработки Similarity", e);
        }
    }
}
