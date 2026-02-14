package com.fiap.brain.health.api.dto.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fiap.brain.health.api.dto.response.ArticleResponse;

import java.time.LocalDateTime;

public record BrainHealthResponseMessage(
        @JsonProperty("messageId")
        String messageId,

        @JsonProperty("userId")
        Long userId,

        @JsonProperty("goalId")
        Long goalId,

        @JsonProperty("correlationId")
        String correlationId,

        @JsonProperty("articleResponse")
        ArticleResponse articleResponse,

        @JsonProperty("status")
        ProcessingStatus status,

        @JsonProperty("errorMessage")
        String errorMessage,

        @JsonProperty("processedAt")
        LocalDateTime processedAt
) {
    public enum ProcessingStatus {
        SUCCESS,
        FAILED,
    }

    public BrainHealthResponseMessage {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String messageId;
        private Long userId;
        private Long goalId;
        private String correlationId;
        private ArticleResponse articleResponse;
        private ProcessingStatus status;
        private String errorMessage;
        private LocalDateTime processedAt;

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

         public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder goalId(Long goalId) {
            this.goalId = goalId;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder articleResponse(ArticleResponse articleResponse) {
            this.articleResponse = articleResponse;
            return this;
        }

        public Builder status(ProcessingStatus status) {
            this.status = status;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder processedAt(LocalDateTime processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public BrainHealthResponseMessage build() {
            return new BrainHealthResponseMessage(
                    messageId,
                    userId,
                    goalId,
                    correlationId,
                    articleResponse,
                    status,
                    errorMessage,
                    processedAt
            );
        }
    }
}