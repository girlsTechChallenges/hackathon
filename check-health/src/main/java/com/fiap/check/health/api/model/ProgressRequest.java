package com.fiap.check.health.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Progress update request")
public class ProgressRequest {
    
    @NotNull
    @JsonProperty("increment")
    private Integer increment;
    
    @JsonProperty("unit")
    private String unit;
}