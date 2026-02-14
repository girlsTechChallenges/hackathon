package com.fiap.brain.health.application.usecase;

import com.fiap.brain.health.domain.exception.ArticleNotFoundException;
import com.fiap.brain.health.domain.exception.InsufficientContentException;
import com.fiap.brain.health.domain.model.MedicalArticle;
import com.fiap.brain.health.domain.port.AIProcessingPort;
import com.fiap.brain.health.domain.port.MedicalArticleRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case: Search and Generate Medical Article Content
 * Orchestrates the process of:
 * 1. Searching for medical articles
 * 2. Validating content quality
 * 3. Processing with AI to generate structured content
 * Business Rules:
 * - Article must exist for the given topic
 * - Article must have minimum 100 characters
 * - Article should be from trusted source (warning only)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchAndGenerateArticleUseCase {

    private static final int MINIMUM_CONTENT_LENGTH = 100;

    private final MedicalArticleRepositoryPort articleRepository;
    private final AIProcessingPort aiProcessing;

    public AIProcessingPort.AIProcessingResult execute(String question) {
        log.info("Executing use case: Search and Generate Article - Question: {}", question);

        try {
            // Step 1: Find article
            MedicalArticle article = articleRepository.findByTopic(question)
                    .orElseThrow(() -> ArticleNotFoundException.forTopic(question));

            // Step 2: Validate content quality
            validateArticleContent(article);

            // Step 3: Check source trustworthiness (warning only, not blocking)
            if (!article.isFromTrustedSource()) {
                log.warn("Article from untrusted source: {} - Proceeding anyway", article.getArticleUrl());
            }

            // Step 4: Process with AI
            AIProcessingPort.AIProcessingResult result = aiProcessing.processArticle(question, article);

            log.info("Use case completed successfully - Title: {}", result.title());
            return result;

        } catch (ArticleNotFoundException e) {
            log.warn("Article not found for topic: {}", question);
            throw e;
        } catch (InsufficientContentException e) {
            log.error("Insufficient article content: {} characters (minimum: {})",
                e.getActualLength(), e.getMinimumLength());
            throw e;
        } catch (AIProcessingPort.AIProcessingException e) {
            log.error("AI processing failed: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in use case: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute article generation use case", e);
        }
    }

    private void validateArticleContent(MedicalArticle article) {
        if (!article.hasMinimumContent(MINIMUM_CONTENT_LENGTH)) {
            throw new InsufficientContentException(
                article.getContentLength(),
                MINIMUM_CONTENT_LENGTH
            );
        }
    }
}
