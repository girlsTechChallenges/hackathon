package com.fiap.brain.health.domain.model;

import java.util.Optional;

public class MedicalArticle {

    private final String content;
    private final String articleUrl;
    private final ArticleMetadata metadata;

    public MedicalArticle(String content, String articleUrl) {
        this(content, articleUrl, ArticleMetadata.empty());
    }

    public MedicalArticle(String content, String articleUrl, ArticleMetadata metadata) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Article content cannot be null or blank");
        }
        if (articleUrl == null || articleUrl.isBlank()) {
            throw new IllegalArgumentException("Article URL cannot be null or blank");
        }

        this.content = content;
        this.articleUrl = articleUrl;
        this.metadata = metadata != null ? metadata : ArticleMetadata.empty();
    }

    public boolean hasMinimumContent(int minLength) {
        return content.length() >= minLength;
    }

    public String getSummarizedContent(int maxLength) {
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    public boolean isFromTrustedSource() {
        return articleUrl != null &&
               (articleUrl.contains("cremesp.org.br") ||
                articleUrl.contains("pubmed.gov") ||
                articleUrl.contains("scielo.br"));
    }

    public int getContentLength() {
        return content.length();
    }

    public String getContent() {
        return content;
    }

    public String getArticleUrl() {
        return articleUrl;
    }

    public ArticleMetadata getMetadata() {
        return metadata;
    }

    public static Optional<MedicalArticle> of(String content, String articleUrl) {
        try {
            return Optional.of(new MedicalArticle(content, articleUrl));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public record ArticleMetadata(String source, String author, String publishDate) {
        public static ArticleMetadata empty() {
            return new ArticleMetadata("Unknown", "Unknown", "Unknown");
        }
    }
}
