package com.fiap.brain.health.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

public record ArticleResponse(
        String title,
        String introduction,
        List<Recommendation> recommendations,
        String conclusion,
        List<Quiz> quizzes,
        String sourceLink,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp
) {

    public ArticleResponse {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank");
        }

        recommendations = recommendations != null ? List.copyOf(recommendations) : List.of();
        quizzes = quizzes != null ? List.copyOf(quizzes) : List.of();
        timestamp = (timestamp == null) ? LocalDateTime.now() : timestamp;
    }

    public static ArticleResponse create(
            String title,
            String introduction,
            List<Recommendation> recommendations,
            String conclusion,
            List<Quiz> quizzes,
            String sourceLink
    ) {
        return new ArticleResponse(
                title,
                introduction,
                recommendations,
                conclusion,
                quizzes,
                sourceLink,
                LocalDateTime.now()
        );
    }
}
