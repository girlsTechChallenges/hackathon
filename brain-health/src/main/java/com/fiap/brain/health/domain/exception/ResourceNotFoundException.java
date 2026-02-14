package com.fiap.brain.health.domain.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends DomainException {

    private final String resourceType;
    private final String resourceIdentifier;

    public ResourceNotFoundException(String resourceType, String resourceIdentifier) {
        super(String.format("%s not found with identifier: %s", resourceType, resourceIdentifier));
        this.resourceType = resourceType;
        this.resourceIdentifier = resourceIdentifier;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = "Resource";
        this.resourceIdentifier = "unknown";
    }
}
