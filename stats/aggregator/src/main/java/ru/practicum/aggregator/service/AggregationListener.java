package ru.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import ru.practicum.aggregator.config.KafkaAggregatorConfig;
import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.avro.UserActionAvro;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregationListener {

    private final EventsSimilarityService eventsSimilarityService;
    private final KafkaAggregatorConfig kafkaAggregatorConfig;
    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    @KafkaListener(
            topics = "#{kafkaAggregatorConfig.consumer.topic}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(
            UserActionAvro userAction,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack
    ) {
        log.trace("Получено сообщение: partition={}, offset={}", partition, offset);

        try {
            List<EventSimilarityAvro> similarities = eventsSimilarityService.countSimilarity(userAction);

            for (EventSimilarityAvro similarity : similarities) {
                String key = similarity.getEventA() + "_" + similarity.getEventB();

                kafkaTemplate.send(
                        kafkaAggregatorConfig.getProducer().getTopic(),
                        null,
                        similarity.getTimestamp().toEpochMilli(),
                        key,
                        similarity
                ).whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Ошибка отправки в топик {}",
                                kafkaAggregatorConfig.getProducer().getTopic(), ex);
                        return;
                    }

                    var md = result.getRecordMetadata();
                    log.info("Отправлено: topic={}, partition={}, offset={}",
                            md.topic(), md.partition(), md.offset());
                });
            }

            ack.acknowledge();

        } catch (Exception e) {
            log.error("Ошибка обработки сообщения", e);
        }
    }
}

