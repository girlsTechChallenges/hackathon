package com.fiap.brain.health.infrastructure.exception;

/**
 * Base exception for infrastructure layer errors.
 * Use this as parent for all infrastructure-related exceptions
 * (database, network, messaging, external services).
 */
public class InfrastructureException extends RuntimeException {

    public InfrastructureException(String message) {
        super(message);
    }

    public InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
