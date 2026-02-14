package com.fiap.brain.health.api.dto.response;

import java.util.List;
import java.util.Objects;

public record Recommendation(
    String category,
    String description,
    List<String> tips
) {

    public Recommendation {
        Objects.requireNonNull(category, "Category cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");

        if (category.isBlank()) {
            throw new IllegalArgumentException("Category cannot be blank");
        }
        if (description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }

        tips = tips != null ? List.copyOf(tips) : List.of();
    }
}
