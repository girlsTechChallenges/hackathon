package com.fiap.brain.health.api.dto.error;

import java.util.List;

public record ValidationError(
        String field,
        String message,
        Object rejectedValue,
        List<String> constraints
) {
    public ValidationError(String field, String message) {
        this(field, message, null, null);
    }
}
