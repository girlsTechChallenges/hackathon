package com.fiap.brain.health.infrastructure.adapter.kafka;

import com.fiap.brain.health.api.dto.kafka.BrainHealthRequestMessage;
import com.fiap.brain.health.api.dto.kafka.BrainHealthResponseMessage;
import com.fiap.brain.health.application.usecase.ProcessKafkaMessageUseCase;
import com.fiap.brain.health.domain.exception.InvalidMessageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrainHealthKafkaConsumer {

    private final ProcessKafkaMessageUseCase processKafkaMessageUseCase;
    private final BrainHealthKafkaProducer kafkaProducer;

    @KafkaListener(
            topics = "${kafka.topic.consumer}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload BrainHealthRequestMessage message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        String traceId = UUID.randomUUID().toString();

        // Enriquece mensagem com messageId e correlationId se não existirem
        BrainHealthRequestMessage enrichedMessage = enrichMessage(message, traceId);

        log.info("[{}] Kafka message received - partition: {}, offset: {}, goalId: {}, userId: {}, title: {}",
                traceId, partition, offset, enrichedMessage.goalId(), enrichedMessage.userId(), enrichedMessage.title());

        try {
            // Validate message
            validateMessage(enrichedMessage);

            // Process message
            BrainHealthResponseMessage response = processKafkaMessageUseCase.process(enrichedMessage);
            String key = processKafkaMessageUseCase.resolveKey(enrichedMessage);

            // Send response
            kafkaProducer.sendResponse(key, response);

            // Acknowledge message
            acknowledgment.acknowledge();

            log.info("[{}] Kafka message processed successfully - goalId: {}, userId: {}, status: {}",
                    traceId, enrichedMessage.goalId(), enrichedMessage.userId(), response.status());

        } catch (InvalidMessageException e) {
            log.error("[{}] Message validation failed - goalId: {}, userId: {}, error: {}",
                traceId, enrichedMessage.goalId(), enrichedMessage.userId(), e.getMessage());

            BrainHealthResponseMessage errorResponse = buildValidationErrorResponse(enrichedMessage, e.getMessage());
            kafkaProducer.sendResponse(processKafkaMessageUseCase.resolveKey(enrichedMessage), errorResponse);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("[{}] Message processing failed - goalId: {}, userId: {}, error: {}",
                traceId, enrichedMessage.goalId(), enrichedMessage.userId(), e.getMessage(), e);

            BrainHealthResponseMessage errorResponse = buildProcessingErrorResponse(enrichedMessage, e.getMessage());
            kafkaProducer.sendResponse(processKafkaMessageUseCase.resolveKey(enrichedMessage), errorResponse);
            acknowledgment.acknowledge();
        }
    }

    /**
     * Enriquece a mensagem com messageId e correlationId se não existirem
     */
    private BrainHealthRequestMessage enrichMessage(BrainHealthRequestMessage message, String traceId) {
        String messageId = message.messageId() != null ? message.messageId() : traceId;
        String correlationId = message.correlationId() != null ? message.correlationId() : traceId;
        java.time.LocalDateTime requestedAt = message.requestedAt() != null ? message.requestedAt() : java.time.LocalDateTime.now();

        return new BrainHealthRequestMessage(
            message.goalId(),
            message.userId(),
            message.category(),
            message.title(),
            message.description(),
            messageId,
            correlationId,
            requestedAt
        );
    }

    /**
     * Validates incoming Kafka message.
     * Throws InvalidMessageException if validation fails.
     */
    private void validateMessage(BrainHealthRequestMessage message) {
        // userId é obrigatório
        if (message.userId() == null) {
            throw InvalidMessageException.missingField("userId");
        }

        // title é obrigatório - usado para buscar o artigo
        if (message.title() == null || message.title().isBlank()) {
            throw InvalidMessageException.missingField("title");
        }

        // Validação adicional do title
        if (message.title().length() < 3) {
            throw new InvalidMessageException("title", "Title must be at least 3 characters long");
        }
        if (message.title().length() > 500) {
            throw new InvalidMessageException("title", "Title must not exceed 500 characters");
        }
    }

    private BrainHealthResponseMessage buildValidationErrorResponse(
            BrainHealthRequestMessage request, String errorMessage) {
        return BrainHealthResponseMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .userId(request.userId())
                .correlationId(request.correlationId())
                .status(BrainHealthResponseMessage.ProcessingStatus.FAILED)
                .errorMessage("Validation error: " + errorMessage)
                .processedAt(LocalDateTime.now())
                .build();
    }

    private BrainHealthResponseMessage buildProcessingErrorResponse(
            BrainHealthRequestMessage request, String errorMessage) {
        return BrainHealthResponseMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .userId(request.userId())
                .correlationId(request.correlationId())
                .status(BrainHealthResponseMessage.ProcessingStatus.FAILED)
                .errorMessage("Processing error: " + errorMessage)
                .processedAt(LocalDateTime.now())
                .build();
    }
}
