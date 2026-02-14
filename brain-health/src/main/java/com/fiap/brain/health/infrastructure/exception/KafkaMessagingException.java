package com.fiap.brain.health.infrastructure.exception;

import lombok.Getter;

/**
 * Exception thrown when Kafka messaging operations fail.
 * Use this for Kafka-specific errors (e.g., serialization failure,
 * broker unavailable, topic not found).
 * HTTP Status: 503 Service Unavailable (if exposed via API)
 */
@Getter
public class KafkaMessagingException extends InfrastructureException {

    private final String topic;
    private final String operation;

    public KafkaMessagingException(String topic, String operation, String message) {
        super(String.format("Kafka %s failed for topic '%s': %s", operation, topic, message));
        this.topic = topic;
        this.operation = operation;
    }

    public KafkaMessagingException(String topic, String operation, String message, Throwable cause) {
        super(String.format("Kafka %s failed for topic '%s': %s", operation, topic, message), cause);
        this.topic = topic;
        this.operation = operation;
    }
}
