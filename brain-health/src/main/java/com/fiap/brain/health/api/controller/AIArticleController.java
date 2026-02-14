package com.fiap.brain.health.api.controller;

import com.fiap.brain.health.api.controller.docs.AIArticleControllerDoc;
import com.fiap.brain.health.api.dto.kafka.BrainHealthResponseMessage;
import com.fiap.brain.health.api.dto.request.AIArticleRequest;
import com.fiap.brain.health.api.dto.response.ArticleResponse;
import com.fiap.brain.health.application.mapper.ArticleResponseMapper;
import com.fiap.brain.health.application.usecase.SearchAndGenerateArticleUseCase;
import com.fiap.brain.health.domain.model.MedicalArticle;
import com.fiap.brain.health.domain.port.AIProcessingPort;
import com.fiap.brain.health.domain.port.MedicalArticleRepositoryPort;
import com.fiap.brain.health.infrastructure.adapter.kafka.BrainHealthKafkaProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai/articles")
@RequiredArgsConstructor
@Validated
public class AIArticleController implements AIArticleControllerDoc {

    private final SearchAndGenerateArticleUseCase searchAndGenerateUseCase;
    private final ArticleResponseMapper responseMapper;
    private final MedicalArticleRepositoryPort articleRepository;
    private final BrainHealthKafkaProducer kafkaProducer;

    @PostMapping("/search")
    @Override
    public ResponseEntity<ArticleResponse> searchArticle(@Valid @RequestBody AIArticleRequest request) {
        log.info("Received article search request - userId: {}, goalId: {}, title: {}",
                request.userId(), request.goalId(), request.title());

        AIProcessingPort.AIProcessingResult aiResult =
            searchAndGenerateUseCase.execute(request.title());

        MedicalArticle article = articleRepository.findByTopic(request.title())
                .orElseThrow();

        ArticleResponse articleResponse = responseMapper.toArticleResponse(aiResult, article);
        log.info("Article search completed successfully - Title: {}", articleResponse.title());

        BrainHealthResponseMessage kafkaResponse = BrainHealthResponseMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .userId(request.userId())
                .correlationId(UUID.randomUUID().toString())
                .articleResponse(articleResponse)
                .status(BrainHealthResponseMessage.ProcessingStatus.SUCCESS)
                .processedAt(LocalDateTime.now())
                .build();

        String key = String.valueOf(kafkaResponse.userId());
        kafkaProducer.sendResponse(key, kafkaResponse);

        log.info("Article posted to Kafka topic brain-health-response - Title: {}, UserId: {}, Key: {}",
                articleResponse.title(), request.userId(), key);

        return ResponseEntity.ok(articleResponse);
    }
}
