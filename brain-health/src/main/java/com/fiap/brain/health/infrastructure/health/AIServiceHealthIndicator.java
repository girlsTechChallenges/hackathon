package com.fiap.brain.health.infrastructure.health;

import com.fiap.brain.health.domain.port.AIProcessingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

@Slf4j
@Component("aiServiceHealthIndicator")
@RequiredArgsConstructor
@ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
public class AIServiceHealthIndicator implements HealthIndicator {

    private final AIProcessingPort aiProcessingPort;

    @Override
    public Health health() {
        try {
            if (aiProcessingPort != null) {
                log.debug("AI Service health check - service available");
                return Health.up()
                        .withDetail("service", "AI Processing")
                        .withDetail("status", "AVAILABLE")
                        .withDetail("provider", "OpenAI")
                        .build();
            } else {
                log.warn("AI Service health check - service unavailable");
                return Health.down()
                        .withDetail("service", "AI Processing")
                        .withDetail("status", "UNAVAILABLE")
                        .build();
            }

        } catch (Exception e) {
            log.error("AI Service health check - error occurred", e);
            return Health.down()
                    .withDetail("service", "AI Processing")
                    .withDetail("status", "ERROR")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
