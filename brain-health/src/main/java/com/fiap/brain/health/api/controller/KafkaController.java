package com.fiap.brain.health.api.controller;

import com.fiap.brain.health.api.controller.docs.KafkaControllerDoc;
import com.fiap.brain.health.api.dto.kafka.BrainHealthRequestMessage;
import com.fiap.brain.health.api.dto.request.KafkaTestMessageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/kafka")
@RequiredArgsConstructor
public class KafkaController implements KafkaControllerDoc {

    private final KafkaTemplate<String, BrainHealthRequestMessage> requestKafkaTemplate;

    @Value("${kafka.topic.consumer}")
    private String requestTopic;

    @Value("${kafka.topic.producer}")
    private String responseTopic;

    @PostMapping("/test/send")
    @Override
    public ResponseEntity<Map<String, Object>> sendTestMessage(@Valid @RequestBody KafkaTestMessageRequest request) {
        log.info("Sending test message to Kafka - goalId: {}, userId: {}, title: {}",
                request.goalId(), request.userId(), request.title());

        String messageId = UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();

        // Criar mensagem Kafka
        BrainHealthRequestMessage kafkaMessage = BrainHealthRequestMessage.builder()
                .goalId(request.goalId())
                .userId(request.userId())
                .category(request.category())
                .title(request.title())
                .description(request.description())
                .messageId(messageId)
                .correlationId(correlationId)
                .requestedAt(LocalDateTime.now())
                .build();

        // Enviar para o tópico brain-health-request
        String key = String.valueOf(request.userId());
        requestKafkaTemplate.send(requestTopic, key, kafkaMessage)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Test message sent to Kafka successfully - topic: '{}', partition: {}, offset: {}, messageId: {}",
                                requestTopic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                messageId);
                    } else {
                        log.error("Error sending test message to Kafka - topic: '{}', messageId: '{}'",
                                requestTopic, messageId, ex);
                    }
                });

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SENT_TO_KAFKA");
        response.put("message", "Message sent to Kafka topic: " + requestTopic);
        response.put("messageId", messageId);
        response.put("correlationId", correlationId);
        response.put("goalId", request.goalId());
        response.put("userId", request.userId());
        response.put("category", request.category());
        response.put("title", request.title());
        response.put("description", request.description());
        response.put("topic", requestTopic);
        response.put("note", "Message sent! Check " + responseTopic + " topic for the AI-generated article response.");

        log.info("✅ Test message sent - messageId: {}, goalId: {}, userId: {}, topic: {}",
                messageId, request.goalId(), request.userId(), requestTopic);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @Override
    public ResponseEntity<Map<String, String>> getKafkaInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("requestTopic", requestTopic);
        info.put("responseTopic", responseTopic);
        info.put("status", "Kafka integration active");

        return ResponseEntity.ok(info);
    }
}

