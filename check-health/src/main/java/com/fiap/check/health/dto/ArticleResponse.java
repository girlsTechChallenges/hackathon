package com.fiap.check.health.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {

    private String title;
    private String introduction;
    private String conclusion;
    private String sourceLink;
    private String timestamp;

    private List<Recommendation> recommendations;
    private List<Quiz> quizzes;

}
