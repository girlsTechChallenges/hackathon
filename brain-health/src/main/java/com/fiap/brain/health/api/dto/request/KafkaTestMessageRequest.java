package com.fiap.brain.health.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Test message request for Kafka integration")
public record KafkaTestMessageRequest(
        @JsonProperty("goalId")
        @Schema(description = "Goal ID", example = "1")
        Long goalId,

        @JsonProperty("userId")
        @Schema(description = "User ID", example = "12345", required = true)
        Long userId,

        @JsonProperty("category")
        @Schema(description = "Category", example = "SAUDE_FISICA")
        String category,

        @JsonProperty("title")
        @Schema(description = "Title used to search medical article", example = "Benefícios da Caminhada", required = true)
        String title,

        @JsonProperty("description")
        @Schema(description = "Additional description", example = "Artigo sobre os benefícios da caminhada para a saúde")
        String description
) {
    public KafkaTestMessageRequest {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
    }
}
