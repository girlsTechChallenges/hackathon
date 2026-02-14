package com.fiap.check.health.service;

import com.fiap.check.health.api.model.GoalRequest;
import com.fiap.check.health.api.model.GoalResponse;
import com.fiap.check.health.api.model.ProgressRequest;

import java.util.List;
import java.util.Optional;

public interface GoalService {
    
    GoalResponse createGoal(GoalRequest goalRequest);
    
    List<GoalResponse> listGoals();
    
    Optional<GoalResponse> findById(Long goalId);
    
    GoalResponse updateGoal(Long goalId, GoalRequest goalRequest);
    
    void deleteGoal(Long goalId);
    
    GoalResponse updateProgress(Long goalId, ProgressRequest progressRequest);
}