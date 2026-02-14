package com.fiap.check.health.event.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.check.health.dto.event.GoalCreatedEvent;
import com.fiap.check.health.persistence.entity.Goal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class GoalEventPublisher {
    
    private static final String TOPIC_GOAL_CREATED = "goal.created";
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    public GoalEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    public void publishGoalCreated(Goal goal) {
        try {
            GoalCreatedEvent event = GoalCreatedEvent.builder()
                    .goalId(goal.getGoalId())
                    .userId(goal.getUserId())
                    .category(goal.getCategory().name())
                    .title(goal.getTitle())
                    .description(goal.getDescription())
                    .build();
                    
            String eventJson = objectMapper.writeValueAsString(event);
            
            log.info("Enviando evento goal.created para o tópico {} - goalId: {}, userId: {}", 
                    TOPIC_GOAL_CREATED, goal.getGoalId(), goal.getUserId());
                    
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC_GOAL_CREATED, eventJson);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Evento goal.created enviado com sucesso - offset: {}, partition: {}", 
                            result.getRecordMetadata().offset(), 
                            result.getRecordMetadata().partition());
                } else {
                    log.error("Erro ao enviar evento goal.created para o Kafka", ex);
                }
            });
            
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar evento goal.created para JSON", e);
            throw new RuntimeException("Falha ao serializar evento goal.created", e);
        } catch (Exception e) {
            log.error("Erro ao publicar evento goal.created para o Kafka", e);
            throw new RuntimeException("Falha ao publicar evento goal.created", e);
        }
    }
}