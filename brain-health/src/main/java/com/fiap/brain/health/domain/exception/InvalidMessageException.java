package com.fiap.brain.health.domain.exception;

public class InvalidMessageException extends ValidationException {

    public InvalidMessageException(String field, String error) {
        super(field, error);
    }

    public static InvalidMessageException missingField(String field) {
        return new InvalidMessageException(field, field + " is required and cannot be null or blank");
    }
}