package com.fiap.check.health.exception;

public class GoalAlreadyCanceledException extends RuntimeException {
    
    public GoalAlreadyCanceledException(Long goalId) {
        super("Goal is already canceled with ID: " + goalId);
    }
    
    public GoalAlreadyCanceledException(String message) {
        super(message);
    }
}
