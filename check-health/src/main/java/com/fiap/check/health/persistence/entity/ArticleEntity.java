package com.fiap.check.health.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleEntity {

    @Column(name = "article_title", columnDefinition = "TEXT")
    private String title;

    @Lob
    @Column(name = "article_introduction", columnDefinition = "TEXT")
    private String introduction;

    @Lob
    @Column(name = "article_conclusion", columnDefinition = "TEXT")
    private String conclusion;

    @Column(name = "article_source_link", columnDefinition = "TEXT")
    private String sourceLink;

    @Column(name = "article_timestamp", columnDefinition = "TEXT")
    private String timestamp;

    @Lob
    @Column(name = "article_recommendations", columnDefinition = "TEXT")
    private String recommendationsJson;

    @Lob
    @Column(name = "article_quizzes", columnDefinition = "TEXT")
    private String quizzesJson;
}