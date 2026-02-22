package ru.practicum.aggregator.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.avro.UserActionAvro;


@EnableKafka
@Configuration
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "aggregator.kafka")
@Getter
public class KafkaAggregatorConfig {

    private final Consumer consumer = new Consumer();
    private final Producer producer = new Producer();

    @Getter
    @Setter
    public static class Consumer {
        private String topic;
    }

    @Getter
    @Setter
    public static class Producer {
        private String topic;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> kafkaListenerContainerFactory(
            ConsumerFactory<String, UserActionAvro> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setConcurrency(3);
        return factory;
    }

    @Bean
    public KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate(
            ProducerFactory<String, EventSimilarityAvro> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }
}
