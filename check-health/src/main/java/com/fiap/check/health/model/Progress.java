package com.fiap.check.health.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Progress {
    
    private Integer completed;
    
    private Integer total;
    
    private String unit; // days, weeks etc.
}
