package com.fiap.brain.health.application.usecase;

import com.fiap.brain.health.api.dto.kafka.BrainHealthRequestMessage;
import com.fiap.brain.health.api.dto.kafka.BrainHealthResponseMessage;
import com.fiap.brain.health.application.mapper.ArticleResponseMapper;
import com.fiap.brain.health.domain.exception.ArticleNotFoundException;
import com.fiap.brain.health.domain.exception.InsufficientContentException;
import com.fiap.brain.health.domain.model.MedicalArticle;
import com.fiap.brain.health.domain.port.AIProcessingPort;
import com.fiap.brain.health.domain.port.MedicalArticleRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Use Case: Process Kafka Message
 * Handles incoming Kafka messages from brain-health-request topic
 * and generates responses for brain-health-response topic.
 * Responsibilities:
 * - Process article search requests from Kafka
 * - Generate AI-powered responses
 * - Handle errors gracefully with proper error responses
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessKafkaMessageUseCase {

    private static final int MINIMUM_CONTENT_LENGTH = 100;

    private final MedicalArticleRepositoryPort articleRepository;
    private final AIProcessingPort aiProcessing;
    private final ArticleResponseMapper responseMapper;

    public BrainHealthResponseMessage process(BrainHealthRequestMessage request) {
        log.info("Processing Kafka message - messageId: {}, correlationId: {}, title: {}",
                request.messageId(), request.correlationId(), request.title());

        try {
            // Usa o campo 'title' da mensagem Kafka para buscar o artigo
            MedicalArticle article = articleRepository.findByTopic(request.title())
                    .orElseThrow(() -> ArticleNotFoundException.forTopic(request.title()));

            if (!article.hasMinimumContent(MINIMUM_CONTENT_LENGTH)) {
                log.warn("Article content too short for title: {}", request.title());
                throw new InsufficientContentException(
                    article.getContentLength(),
                    MINIMUM_CONTENT_LENGTH
                );
            }

            // Processa com IA usando o title
            AIProcessingPort.AIProcessingResult aiResult =
                    aiProcessing.processArticle(request.title(), article);

            var articleResponse = responseMapper.toArticleResponse(aiResult, article);

            return buildSuccessResponse(request, articleResponse);

        } catch (ArticleNotFoundException e) {
            log.warn("Article not found for Kafka message title '{}': {}", request.title(), e.getMessage());
            return buildErrorResponse(request, "Article not found: " + e.getMessage());

        } catch (InsufficientContentException e) {
            log.warn("Insufficient content for Kafka message title '{}': {}", request.title(), e.getMessage());
            return buildErrorResponse(request, e.getMessage());

        } catch (AIProcessingPort.AIProcessingException e) {
            log.error("AI processing failed for Kafka message title '{}': {}", request.title(), e.getMessage(), e);
            return buildErrorResponse(request, "AI processing error: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error processing Kafka message title '{}': {}", request.title(), e.getMessage(), e);
            return buildErrorResponse(request, "Unexpected error: " + e.getMessage());
        }
    }

    private BrainHealthResponseMessage buildSuccessResponse(
            BrainHealthRequestMessage request,
            com.fiap.brain.health.api.dto.response.ArticleResponse articleResponse) {

        return BrainHealthResponseMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .userId(request.userId())
                .correlationId(request.correlationId())
                .articleResponse(articleResponse)
                .status(BrainHealthResponseMessage.ProcessingStatus.SUCCESS)
                .processedAt(LocalDateTime.now())
                .build();
    }

    private BrainHealthResponseMessage buildErrorResponse(
            BrainHealthRequestMessage request,
            String errorMessage) {

        return BrainHealthResponseMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .userId(request.userId())
                .correlationId(request.correlationId())
                .status(BrainHealthResponseMessage.ProcessingStatus.FAILED)
                .errorMessage(errorMessage)
                .processedAt(LocalDateTime.now())
                .build();
    }

    public String resolveKey(BrainHealthRequestMessage request) {
        // Usa correlationId se disponível, senão usa userId
        return Optional.ofNullable(request.correlationId())
                .orElse(String.valueOf(request.userId()));
    }
}
