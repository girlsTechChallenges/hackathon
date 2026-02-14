package com.fiap.check.health.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fiap.check.health.dto.ArticleResponse;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Article {

    private String messageId;
    private Long userId;
    private Long goalId;
    private String correlationId;
    private String status;
    private String errorMessage;
    private ArticleResponse articleResponse; 

}