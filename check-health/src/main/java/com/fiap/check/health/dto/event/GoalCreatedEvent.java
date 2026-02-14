package com.fiap.check.health.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalCreatedEvent {
    
    @JsonProperty("goalId")
    private Long goalId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
}