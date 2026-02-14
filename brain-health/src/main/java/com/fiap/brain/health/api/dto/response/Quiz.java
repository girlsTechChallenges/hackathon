package com.fiap.brain.health.api.dto.response;

import java.util.List;

public record Quiz(
    String question,
    List<String> options,
    String correctAnswer
) {
    public Quiz {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question cannot be null or blank");
        }
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Options cannot be null or empty");
        }
        if (correctAnswer == null || correctAnswer.isBlank()) {
            throw new IllegalArgumentException("Correct answer cannot be null or blank");
        }
    }
}
