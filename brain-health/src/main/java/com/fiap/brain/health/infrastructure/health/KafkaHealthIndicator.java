package com.fiap.brain.health.infrastructure.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

@Slf4j
@Component("kafkaHealthIndicator")
@RequiredArgsConstructor
@ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
public class KafkaHealthIndicator implements HealthIndicator {

    private final KafkaAdmin kafkaAdmin;

    @Override
    public Health health() {
        try {
            var config = kafkaAdmin.getConfigurationProperties();
            log.debug("Kafka health check - connected successfully");

            return Health.up()
                    .withDetail("bootstrapServers", config.get("bootstrap.servers"))
                    .withDetail("status", "CONNECTED")
                    .build();

        } catch (Exception e) {
            log.error("Kafka health check - connection failed", e);

            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("status", "DISCONNECTED")
                    .build();
        }
    }
}
