package ru.practicum.aggregator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "similarity")
@Getter
@Setter
public class SimilarityConfig {
    private Map<String, Double> actionWeights;
}
