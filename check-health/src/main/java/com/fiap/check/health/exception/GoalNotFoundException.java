package com.fiap.check.health.exception;

public class GoalNotFoundException extends RuntimeException {
    
    public GoalNotFoundException(Long goalId) {
        super("Goal não encontrado com ID: " + goalId);
    }
    
    public GoalNotFoundException(String message) {
        super(message);
    }
}