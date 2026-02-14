package com.fiap.check.health.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fiap.check.health.api.DefaultApi;
import com.fiap.check.health.api.model.GoalRequest;
import com.fiap.check.health.api.model.GoalResponse;
import com.fiap.check.health.api.model.ProgressRequest;
import com.fiap.check.health.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GoalController implements DefaultApi {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @Override
    public ResponseEntity<GoalResponse> goalsPost(@Valid GoalRequest goalRequest) throws JsonProcessingException {
        GoalResponse response = goalService.createGoal(goalRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<List<GoalResponse>> goalsGet() throws JsonProcessingException {
        List<GoalResponse> responses = goalService.listGoals();
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<GoalResponse> goalsGoalIdGet(String goalId) {
        return goalService.findById(Long.parseLong(goalId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<GoalResponse> goalsGoalIdPut(String goalId, @Valid GoalRequest goalRequest) {
        GoalResponse response = goalService.updateGoal(Long.parseLong(goalId), goalRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> goalsGoalIdDelete(String goalId) {
        goalService.deleteGoal(Long.parseLong(goalId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<GoalResponse> goalsGoalIdProgressPatch(String goalId, @Valid ProgressRequest progressRequest) {
        GoalResponse response = goalService.updateProgress(
                Long.parseLong(goalId), 
                progressRequest
        );
        return ResponseEntity.ok(response);
    }
}