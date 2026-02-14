package com.fiap.brain.health.domain.exception;

/**
 * Exception lançada quando ocorre um erro no processamento via AI.
 * Indica problemas na comunicação ou processamento com o serviço de IA.
 */
public class AIProcessingException extends DomainException {

    public AIProcessingException(String message) {
        super(message);
    }

    public AIProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
