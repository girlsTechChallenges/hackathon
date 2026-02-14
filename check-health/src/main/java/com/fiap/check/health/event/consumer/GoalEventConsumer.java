package com.fiap.check.health.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.check.health.dto.ArticleResponse;
import com.fiap.check.health.model.Article;
import com.fiap.check.health.persistence.entity.ArticleEntity;
import com.fiap.check.health.persistence.entity.Goal;
import com.fiap.check.health.persistence.repository.GoalRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class GoalEventConsumer {

    private final ObjectMapper objectMapper;
    private final GoalRepository goalRepository;

    public GoalEventConsumer(ObjectMapper objectMapper, GoalRepository goalRepository) {
        this.objectMapper = objectMapper;
        this.goalRepository = goalRepository;
    }

    @KafkaListener(topics = "goal.progress.updated", groupId = "goal-progress-consumers")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            String messageJson = record.value();

            // DTO do evento Kafka
            Article event = objectMapper.readValue(messageJson, Article.class);

            log.info("Received goal.progress.updated event - messageId: {}, userId: {}, status: {}",
                    event.getMessageId(), event.getUserId(), event.getStatus());

            // Busca todas as metas do usuário
            List<Goal> goals = goalRepository.findByUserId(String.valueOf(event.getUserId()));

            // Filtra pelo goalId do ArticleResponse
            Optional<Goal> goalOpt = goals.stream().filter(g -> g.getGoalId().equals(event.getGoalId())).findFirst();

            log.info("Event goalId: {}", event.getGoalId());
            goals.forEach(g -> log.info("Goal goalId in DB: {}", g.getGoalId()));

            if (goalOpt.isPresent()) {
                Goal goal = goalOpt.get();

                log.info("Updating Goal for userId {} - goalId: {}, title: {}",
                        event.getUserId(), goal.getGoalId(), goal.getTitle());

                // Converter DTO para embeddable JPA
                ArticleResponse dto = event.getArticleResponse();
                ArticleEntity entityResponse = ArticleEntity.builder()
                        .title(dto.getTitle())
                        .introduction(dto.getIntroduction())
                        .conclusion(dto.getConclusion())
                        .sourceLink(dto.getSourceLink())
                        .timestamp(dto.getTimestamp())
                        .recommendationsJson(objectMapper.writeValueAsString(dto.getRecommendations()))
                        .quizzesJson(objectMapper.writeValueAsString(dto.getQuizzes()))
                        .build();

                // Atualiza meta
                goal.setArticleResponse(entityResponse);
                goal.setTitle(dto.getTitle());

                goalRepository.save(goal);

                log.info("Goal {} updated with AI response successfully.", goal.getGoalId());
            } else {
                log.warn("No Goal found for userId {} with goalId {}",
                        event.getUserId(), event.getGoalId());
            }

        } catch (Exception e) {
            log.error("Error while processing goal.progress.updated message", e);
        }
    }
}