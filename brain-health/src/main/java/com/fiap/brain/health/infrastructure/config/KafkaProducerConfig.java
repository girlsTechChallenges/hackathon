package com.fiap.brain.health.infrastructure.config;

import com.fiap.brain.health.api.dto.kafka.BrainHealthRequestMessage;
import com.fiap.brain.health.api.dto.kafka.BrainHealthResponseMessage;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> getCommonProducerConfig() {
        Map<String, Object> configProps = new HashMap<>();

        // Connection
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Reliability configuration
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // Performance configuration
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        // JSON serializer configuration
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return configProps;
    }

    @Bean
    public ProducerFactory<String, BrainHealthResponseMessage> producerFactory() {
        return new DefaultKafkaProducerFactory<>(getCommonProducerConfig());
    }

    @Bean
    public KafkaTemplate<String, BrainHealthResponseMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, BrainHealthRequestMessage> requestProducerFactory() {
        return new DefaultKafkaProducerFactory<>(getCommonProducerConfig());
    }

    @Bean
    public KafkaTemplate<String, BrainHealthRequestMessage> requestKafkaTemplate() {
        return new KafkaTemplate<>(requestProducerFactory());
    }
}
