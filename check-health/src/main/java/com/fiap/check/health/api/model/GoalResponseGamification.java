package com.fiap.check.health.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Goal gamification information")
public class GoalResponseGamification {
    
    @JsonProperty("points_earned")
    private Integer pointsEarned;
    
    @JsonProperty("badge")
    private String badge;
    
    @JsonProperty("user_level")
    private Integer userLevel;
}