package com.fiap.brain.health.domain.port;

import com.fiap.brain.health.domain.model.MedicalArticle;

import java.time.LocalDateTime;
import java.util.List;

public interface AIProcessingPort {

    AIProcessingResult processArticle(String question, MedicalArticle article);

    record AIProcessingResult(
            String title,
            String introduction,
            List<RecommendationItem> recommendations,
            String conclusion,
            List<QuizItem> quizzes,
            LocalDateTime processedAt
    ) {
        public AIProcessingResult {
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("AI processing result must have a title");
            }
            recommendations = recommendations != null ? List.copyOf(recommendations) : List.of();
            quizzes = quizzes != null ? List.copyOf(quizzes) : List.of();
            processedAt = processedAt != null ? processedAt : LocalDateTime.now();
        }
    }

    record RecommendationItem(
            String category,
            String description,
            List<String> tips
    ) {
        public RecommendationItem {
            if (category == null || category.isBlank()) {
                throw new IllegalArgumentException("Recommendation must have a category");
            }
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("Recommendation must have a description");
            }
            tips = tips != null ? List.copyOf(tips) : List.of();
        }
    }

    record QuizItem(
            String question,
            List<String> options,
            String correctAnswer
    ) {
        public QuizItem {
            if (question == null || question.isBlank()) {
                throw new IllegalArgumentException("Quiz must have a question");
            }
            if (options == null || options.isEmpty()) {
                throw new IllegalArgumentException("Quiz must have options");
            }
            if (correctAnswer == null || correctAnswer.isBlank()) {
                throw new IllegalArgumentException("Quiz must have a correct answer");
            }
            options = List.copyOf(options);
        }
    }

    class AIProcessingException extends RuntimeException {
        public AIProcessingException(String message) {
            super(message);
        }

        public AIProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
