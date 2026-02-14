package com.fiap.check.health.dto;

import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {

    private String question;
    private List<String> options;
    private String correctAnswer;
    
}
