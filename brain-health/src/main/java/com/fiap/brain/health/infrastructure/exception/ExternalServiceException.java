package com.fiap.brain.health.infrastructure.exception;

import lombok.Getter;

/**
 * Exception thrown when external service integration fails.
 * Use this for issues with external services (e.g., CREMESP timeout,
 * OpenAI API error, network issues).
 * HTTP Status: 502 Bad Gateway or 503 Service Unavailable
 */
@Getter
public class ExternalServiceException extends InfrastructureException {

    private final String serviceName;
    private final boolean retryable;

    public ExternalServiceException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
        this.retryable = true;
    }

    public ExternalServiceException(String serviceName, String message, boolean retryable) {
        super(message);
        this.serviceName = serviceName;
        this.retryable = retryable;
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.retryable = true;
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.serviceName = serviceName;
        this.retryable = retryable;
    }
}
