package com.fiap.brain.health.infrastructure.health;

import com.fiap.brain.health.domain.port.AIProcessingPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("AIServiceHealthIndicator - Testes Minimalistas")
class AIServiceHealthIndicatorTest {

    @Mock
    private AIProcessingPort aiProcessingPort;

    @InjectMocks
    private AIServiceHealthIndicator aiServiceHealthIndicator;

    @Test
    @DisplayName("Deve retornar UP quando AIServicePort não é nulo")
    void shouldReturnUpWhenPortIsNotNull() {
        AIServiceHealthIndicator indicator = new AIServiceHealthIndicator(aiProcessingPort);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("service", "AI Processing");
        assertThat(health.getDetails()).containsEntry("status", "AVAILABLE");
        assertThat(health.getDetails()).containsEntry("provider", "OpenAI");
    }


    @Test
    @DisplayName("Deve retornar DOWN quando AIServicePort é nulo")
    void shouldReturnDownWhenPortIsNull() {
        AIServiceHealthIndicator indicator = new AIServiceHealthIndicator(null);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("service", "AI Processing");
        assertThat(health.getDetails()).containsEntry("status", "UNAVAILABLE");
    }
}
