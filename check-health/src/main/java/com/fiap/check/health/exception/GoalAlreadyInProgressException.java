package com.fiap.check.health.exception;

public class GoalAlreadyInProgressException extends RuntimeException {
    
    public GoalAlreadyInProgressException(Long goalId) {
        super("Goal is already in progress with ID: " + goalId);
    }
    
    public GoalAlreadyInProgressException(String message) {
        super(message);
    }
}
