package com.fiap.brain.health.infrastructure.adapter.kafka;

import com.fiap.brain.health.api.dto.kafka.BrainHealthRequestMessage;
import com.fiap.brain.health.api.dto.kafka.BrainHealthResponseMessage;
import com.fiap.brain.health.application.usecase.ProcessKafkaMessageUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

class BrainHealthKafkaConsumerTest {

    @Mock
    private ProcessKafkaMessageUseCase processKafkaMessageUseCase;

    @Mock
    private BrainHealthKafkaProducer kafkaProducer;

    @Mock
    private Acknowledgment acknowledgment;

    private BrainHealthKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        consumer = new BrainHealthKafkaConsumer(processKafkaMessageUseCase, kafkaProducer);
    }

    @Test
    void shouldProcessValidMessageSuccessfully() {
        BrainHealthRequestMessage request = new BrainHealthRequestMessage(
                1L, 1L, "SAUDE_FISICA", "Valid Title", "Description",
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), LocalDateTime.now()
        );

        BrainHealthResponseMessage response = BrainHealthResponseMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .status(BrainHealthResponseMessage.ProcessingStatus.SUCCESS)
                .build();

        when(processKafkaMessageUseCase.process(any())).thenReturn(response);
        when(processKafkaMessageUseCase.resolveKey(any())).thenReturn("key-123");

        consumer.consume(request, 0, 0L, acknowledgment);

        verify(kafkaProducer).sendResponse(eq("key-123"), eq(response));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldHandleValidationError() {
        // Cria mensagem com userId válido mas title inválido (vazio)
        BrainHealthRequestMessage invalidRequest = new BrainHealthRequestMessage(
                1L, 1L, "SAUDE_FISICA", "AB", "Description",
                null, null, null
        );

        // O consumer vai capturar a exceção de validação internamente
        when(processKafkaMessageUseCase.resolveKey(any())).thenReturn("key-123");

        consumer.consume(invalidRequest, 0, 0L, acknowledgment);

        ArgumentCaptor<BrainHealthResponseMessage> captor = ArgumentCaptor.forClass(BrainHealthResponseMessage.class);
        verify(kafkaProducer).sendResponse(eq("key-123"), captor.capture());
        verify(acknowledgment).acknowledge();

        BrainHealthResponseMessage errorResponse = captor.getValue();
        assert errorResponse.status() == BrainHealthResponseMessage.ProcessingStatus.FAILED;
        assert errorResponse.errorMessage().contains("Validation error");
    }

    @Test
    void shouldHandleProcessingError() {
        BrainHealthRequestMessage request = new BrainHealthRequestMessage(
                1L, 1L, "SAUDE_FISICA", "Valid Title", "Description",
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), LocalDateTime.now()
        );

        when(processKafkaMessageUseCase.process(any())).thenThrow(new RuntimeException("Unexpected error"));
        when(processKafkaMessageUseCase.resolveKey(any())).thenReturn("key-123");

        consumer.consume(request, 0, 0L, acknowledgment);

        ArgumentCaptor<BrainHealthResponseMessage> captor = ArgumentCaptor.forClass(BrainHealthResponseMessage.class);
        verify(kafkaProducer).sendResponse(eq("key-123"), captor.capture());
        verify(acknowledgment).acknowledge();

        BrainHealthResponseMessage errorResponse = captor.getValue();
        assert errorResponse.status() == BrainHealthResponseMessage.ProcessingStatus.FAILED;
        assert errorResponse.errorMessage().contains("Processing error");
    }
}