package com.fiap.brain.health.domain.exception;

import lombok.Getter;

@Getter
public class InsufficientContentException extends BusinessException {

    private final int actualLength;
    private final int minimumLength;

    public InsufficientContentException(int actualLength, int minimumLength) {
        super(
            "INSUFFICIENT_CONTENT",
            String.format("Article content too short. Found %d characters, minimum required: %d",
                actualLength, minimumLength)
        );
        this.actualLength = actualLength;
        this.minimumLength = minimumLength;
    }
}
