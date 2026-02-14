package com.fiap.brain.health.domain.exception;

import java.util.HashMap;
import java.util.Map;

public class ValidationException extends DomainException {

    private final Map<String, String> validationErrors;

    public ValidationException(String message) {
        super(message);
        this.validationErrors = new HashMap<>();
    }

    public ValidationException(String field, String error) {
        super(String.format("Validation failed for field '%s': %s", field, error));
        this.validationErrors = new HashMap<>();
        this.validationErrors.put(field, error);
    }

    public ValidationException(Map<String, String> validationErrors) {
        super("Validation failed: " + validationErrors.toString());
        this.validationErrors = new HashMap<>(validationErrors);
    }

    public Map<String, String> getValidationErrors() {
        return new HashMap<>(validationErrors);
    }

}
