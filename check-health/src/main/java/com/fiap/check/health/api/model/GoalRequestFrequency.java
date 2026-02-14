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
@Schema(description = "Goal frequency information")
public class GoalRequestFrequency {
    
    @JsonProperty("periodicity")
    private String periodicity;
    
    @JsonProperty("times_per_period")
    private Integer timesPerPeriod;
}