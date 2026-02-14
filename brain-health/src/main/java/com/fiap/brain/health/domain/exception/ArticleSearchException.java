package com.fiap.brain.health.domain.exception;

import com.fiap.brain.health.infrastructure.exception.ExternalServiceException;

public class ArticleSearchException extends ExternalServiceException {

    private static final String SERVICE_NAME = "Medical Article Search";

    public ArticleSearchException(String message) {
        super(SERVICE_NAME, message, true);
    }

    public ArticleSearchException(String message, Throwable cause) {
        super(SERVICE_NAME, message, cause, true);
    }

    public ArticleSearchException(String message, boolean retryable) {
        super(SERVICE_NAME, message, retryable);
    }
}
