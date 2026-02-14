package com.fiap.check.health.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GoalProgressUpdatedEvent {
    
    @JsonProperty("goalId")
    private String goalId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("oldProgress")
    private Integer oldProgress;
    
    @JsonProperty("newProgress")
    private Integer newProgress;
    
    @JsonProperty("increment")
    private Integer increment;
    
    @JsonProperty("unit")
    private String unit;
    
    @JsonProperty("total")
    private Integer total;
    
    @JsonProperty("isCompleted")
    private Boolean isCompleted;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}