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
@Schema(description = "Goal progress information")
public class GoalResponseProgress {
    
    @JsonProperty("completed")
    private Integer completed;
    
    @JsonProperty("total")
    private Integer total;
    
    @JsonProperty("unit")
    private String unit;
}