package com.fiap.check.health.exception;

public class GoalAlreadyCompletedException extends RuntimeException {
    
    public GoalAlreadyCompletedException(Long goalId) {
        super("Goal is already completed with ID: " + goalId);
    }
    
    public GoalAlreadyCompletedException(String message) {
        super(message);
    }
}
