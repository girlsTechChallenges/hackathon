package com.fiap.brain.health.api.dto.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BrainHealthRequestMessage(
        @JsonProperty("goalId")
        Long goalId,

        @JsonProperty("userId")
        Long userId,

        @JsonProperty("category")
        String category,

        @JsonProperty("title")
        String title,

        @JsonProperty("description")
        String description,

        // Campos internos (não vêm do JSON externo, gerados pela aplicação)
        String messageId,
        String correlationId,
        LocalDateTime requestedAt
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {        
        private Long userId;
        private Long goalId;
        private String category;
        private String title;
        private String description;
        private String messageId;
        private String correlationId;
        private LocalDateTime requestedAt;

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder goalId(Long goalId) {
            this.goalId = goalId;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder requestedAt(LocalDateTime requestedAt) {
            this.requestedAt = requestedAt;
            return this;
        }

        public BrainHealthRequestMessage build() {
            return new BrainHealthRequestMessage(                    
                    userId,
                    goalId,
                    category,
                    title,
                    description,
                    messageId,
                    correlationId,
                    requestedAt
            );
        }
    }
}
