package com.fiap.check.health.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fiap.check.health.dto.ArticleResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Goal response with full information")
public class GoalResponse {
    
    @JsonProperty("goal_id")
    private String goalId;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
    
    @Valid
    @JsonProperty("progress")
    private GoalResponseProgress progress;
    
    @Valid
    @JsonProperty("gamification")
    private GoalResponseGamification gamification;
    
    @JsonProperty("message")
    private String message;

    private ArticleResponse article;
}