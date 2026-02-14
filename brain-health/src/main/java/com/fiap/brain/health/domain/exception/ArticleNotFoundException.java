package com.fiap.brain.health.domain.exception;

public class ArticleNotFoundException extends ResourceNotFoundException {

    public ArticleNotFoundException(String message) {
        super("Medical Article", message);
    }

    public ArticleNotFoundException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    public static ArticleNotFoundException forTopic(String topic) {
        return new ArticleNotFoundException(topic);
    }
}
