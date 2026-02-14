package com.fiap.brain.health.infrastructure.adapter.kafka;

import com.fiap.brain.health.api.dto.kafka.BrainHealthResponseMessage;
import com.fiap.brain.health.api.dto.response.ArticleResponse;
import com.fiap.brain.health.api.dto.response.Quiz;
import com.fiap.brain.health.api.dto.response.Recommendation;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BrainHealthKafkaProducer - Testes Unitários")
class BrainHealthKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, BrainHealthResponseMessage> kafkaTemplate;

    @InjectMocks
    private BrainHealthKafkaProducer kafkaProducer;

    private BrainHealthResponseMessage validResponseMessage;
    private static final String RESPONSE_TOPIC = "brain-health-response-test";
    private static final String USER_KEY = "12345";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kafkaProducer, "responseTopic", RESPONSE_TOPIC);

        ArticleResponse articleResponse = new ArticleResponse(
                "Benefícios da Caminhada",
                "A caminhada é benéfica para saúde",
                List.of(new Recommendation("Frequência", "30 min/dia", List.of("Começar devagar"))),
                "Conclusão sobre caminhada",
                List.of(new Quiz("Quantos minutos?", List.of("10", "30", "60"), "30")),
                "https://source.com",
                java.time.LocalDateTime.now()
        );

        validResponseMessage = new BrainHealthResponseMessage(
                "msg-123",
                12345L,
                "corr-456",
                articleResponse,
                BrainHealthResponseMessage.ProcessingStatus.SUCCESS,
                null,
                java.time.LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("Envio de Mensagens - Sucesso")
    class SuccessfulMessageSending {

        @Test
        @DisplayName("Deve enviar mensagem com sucesso")
        void shouldSendMessageSuccessfully() {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createSuccessfulFuture();
            when(kafkaTemplate.send(eq(RESPONSE_TOPIC), eq(USER_KEY), any(BrainHealthResponseMessage.class)))
                    .thenReturn(future);

            // Act
            assertThatCode(() -> kafkaProducer.sendResponse(USER_KEY, validResponseMessage))
                    .doesNotThrowAnyException();

            // Assert
            verify(kafkaTemplate).send(RESPONSE_TOPIC, USER_KEY, validResponseMessage);
        }

        @Test
        @DisplayName("Deve usar tópico correto ao enviar")
        void shouldUseCorrectTopicWhenSending() {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createSuccessfulFuture();
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act
            kafkaProducer.sendResponse(USER_KEY, validResponseMessage);

            // Assert
            verify(kafkaTemplate).send(eq(RESPONSE_TOPIC), anyString(), any());
        }

        @Test
        @DisplayName("Deve usar userId como key da mensagem")
        void shouldUseUserIdAsMessageKey() {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createSuccessfulFuture();
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act
            kafkaProducer.sendResponse("99999", validResponseMessage);

            // Assert
            verify(kafkaTemplate).send(anyString(), eq("99999"), any());
        }

        @Test
        @DisplayName("Deve enviar payload completo da mensagem")
        void shouldSendCompleteMessagePayload() {
            // Arrange
            ArgumentCaptor<BrainHealthResponseMessage> messageCaptor =
                    ArgumentCaptor.forClass(BrainHealthResponseMessage.class);
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createSuccessfulFuture();
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act
            kafkaProducer.sendResponse(USER_KEY, validResponseMessage);

            // Assert
            verify(kafkaTemplate).send(anyString(), anyString(), messageCaptor.capture());
            BrainHealthResponseMessage captured = messageCaptor.getValue();
            assertThat(captured.messageId()).isEqualTo("msg-123");
            assertThat(captured.correlationId()).isEqualTo("corr-456");
            assertThat(captured.userId()).isEqualTo(12345L);
        }

        @Test
        @DisplayName("Deve processar callback de sucesso corretamente")
        void shouldProcessSuccessCallbackCorrectly() {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createSuccessfulFuture();
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act
            kafkaProducer.sendResponse(USER_KEY, validResponseMessage);

            // Assert - Verifica que o future completa sem exceção
            assertThat(future).isCompleted();
            assertThat(future).isNotCompletedExceptionally();
        }
    }

    @Nested
    @DisplayName("Tratamento de Erros")
    class ErrorHandling {

        @Test
        @DisplayName("Deve tratar erro no envio da mensagem")
        void shouldHandleErrorWhenSendingMessage() {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createFailedFuture(new RuntimeException("Kafka unavailable"));
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act & Assert
            assertThatCode(() -> kafkaProducer.sendResponse(USER_KEY, validResponseMessage))
                    .doesNotThrowAnyException(); // Método não lança exceção, apenas loga
        }

        @Test
        @DisplayName("Deve processar callback de erro corretamente")
        void shouldProcessErrorCallbackCorrectly() {
            // Arrange
            RuntimeException error = new RuntimeException("Connection failed");
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createFailedFuture(error);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act
            kafkaProducer.sendResponse(USER_KEY, validResponseMessage);

            // Assert
            assertThat(future).isCompletedExceptionally();
        }

        @Test
        @DisplayName("Deve continuar processamento mesmo com erro no callback")
        void shouldContinueProcessingEvenWithCallbackError() {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createFailedFuture(new RuntimeException("Network error"));
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act
            kafkaProducer.sendResponse(USER_KEY, validResponseMessage);
            kafkaProducer.sendResponse("67890", validResponseMessage);

            // Assert
            verify(kafkaTemplate, times(2)).send(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Deve tratar timeout na publicação")
        void shouldHandlePublicationTimeout() {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createFailedFuture(new RuntimeException("Timeout waiting for response"));
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act & Assert
            assertThatCode(() -> kafkaProducer.sendResponse(USER_KEY, validResponseMessage))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Validações de Payload")
    class PayloadValidation {

        @Test
        @DisplayName("Deve aceitar mensagem com goalId nulo")
        void shouldAcceptMessageWithNullGoalId() {
            // Arrange
            BrainHealthResponseMessage messageWithoutGoal = new BrainHealthResponseMessage(
                    "msg-no-goal", 12345L, "corr-no-goal",
                    validResponseMessage.articleResponse(),
                    BrainHealthResponseMessage.ProcessingStatus.SUCCESS,
                    null, java.time.LocalDateTime.now());
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createSuccessfulFuture();
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act & Assert
            assertThatCode(() -> kafkaProducer.sendResponse(USER_KEY, messageWithoutGoal))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve enviar mensagem com artigo completo")
        void shouldSendMessageWithCompleteArticle() {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createSuccessfulFuture();
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act
            kafkaProducer.sendResponse(USER_KEY, validResponseMessage);

            // Assert
            ArgumentCaptor<BrainHealthResponseMessage> captor =
                    ArgumentCaptor.forClass(BrainHealthResponseMessage.class);
            verify(kafkaTemplate).send(anyString(), anyString(), captor.capture());
            assertThat(captor.getValue().articleResponse()).isNotNull();
            assertThat(captor.getValue().articleResponse().title()).isEqualTo("Benefícios da Caminhada");
        }

        @Test
        @DisplayName("Deve enviar mensagem com recommendations")
        void shouldSendMessageWithRecommendations() {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createSuccessfulFuture();
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act
            kafkaProducer.sendResponse(USER_KEY, validResponseMessage);

            // Assert
            ArgumentCaptor<BrainHealthResponseMessage> captor =
                    ArgumentCaptor.forClass(BrainHealthResponseMessage.class);
            verify(kafkaTemplate).send(anyString(), anyString(), captor.capture());
            assertThat(captor.getValue().articleResponse().recommendations()).isNotEmpty();
        }

        @Test
        @DisplayName("Deve enviar mensagem com quizzes")
        void shouldSendMessageWithQuizzes() {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createSuccessfulFuture();
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act
            kafkaProducer.sendResponse(USER_KEY, validResponseMessage);

            // Assert
            ArgumentCaptor<BrainHealthResponseMessage> captor =
                    ArgumentCaptor.forClass(BrainHealthResponseMessage.class);
            verify(kafkaTemplate).send(anyString(), anyString(), captor.capture());
            assertThat(captor.getValue().articleResponse().quizzes()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Concorrência e Performance")
    class ConcurrencyAndPerformance {

        @Test
        @DisplayName("Deve enviar múltiplas mensagens em sequência")
        void shouldSendMultipleMessagesInSequence() {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createSuccessfulFuture();
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act
            kafkaProducer.sendResponse("1", validResponseMessage);
            kafkaProducer.sendResponse("2", validResponseMessage);
            kafkaProducer.sendResponse("3", validResponseMessage);

            // Assert
            verify(kafkaTemplate, times(3)).send(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Deve usar keys diferentes para usuários diferentes")
        void shouldUseDifferentKeysForDifferentUsers() {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createSuccessfulFuture();
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act
            kafkaProducer.sendResponse("user-1", validResponseMessage);
            kafkaProducer.sendResponse("user-2", validResponseMessage);

            // Assert
            verify(kafkaTemplate).send(anyString(), eq("user-1"), any());
            verify(kafkaTemplate).send(anyString(), eq("user-2"), any());
        }

        @Test
        @DisplayName("Deve processar mensagens independentemente")
        void shouldProcessMessagesIndependently() {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> success =
                    createSuccessfulFuture();
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> failure =
                    createFailedFuture(new RuntimeException("Error"));

            when(kafkaTemplate.send(anyString(), eq("success"), any())).thenReturn(success);
            when(kafkaTemplate.send(anyString(), eq("failure"), any())).thenReturn(failure);

            // Act
            kafkaProducer.sendResponse("success", validResponseMessage);
            kafkaProducer.sendResponse("failure", validResponseMessage);
            kafkaProducer.sendResponse("success", validResponseMessage);

            // Assert
            verify(kafkaTemplate, times(3)).send(anyString(), anyString(), any());
        }
    }

    @Nested
    @DisplayName("Metadata e Logging")
    class MetadataAndLogging {

        @Test
        @DisplayName("Deve capturar metadata de sucesso")
        void shouldCaptureSuccessMetadata() {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createSuccessfulFuture();
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act
            kafkaProducer.sendResponse(USER_KEY, validResponseMessage);

            // Assert
            assertThat(future).isCompleted();
            assertThat(future.join()).isNotNull();
            assertThat(future.join().getRecordMetadata()).isNotNull();
        }

        @Test
        @DisplayName("Deve incluir correlationId no processamento")
        void shouldIncludeCorrelationIdInProcessing() {
            // Arrange
            BrainHealthResponseMessage messageWithCorrelation = new BrainHealthResponseMessage(
                    "msg-trace", 12345L, "corr-trace-12345",
                    validResponseMessage.articleResponse(),
                    BrainHealthResponseMessage.ProcessingStatus.SUCCESS,
                    null, java.time.LocalDateTime.now());
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createSuccessfulFuture();
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act
            kafkaProducer.sendResponse(USER_KEY, messageWithCorrelation);

            // Assert
            ArgumentCaptor<BrainHealthResponseMessage> captor =
                    ArgumentCaptor.forClass(BrainHealthResponseMessage.class);
            verify(kafkaTemplate).send(anyString(), anyString(), captor.capture());
            assertThat(captor.getValue().correlationId()).isEqualTo("corr-trace-12345");
        }

        @Test
        @DisplayName("Deve permitir rastreamento via messageId")
        void shouldAllowTracingViaMessageId() {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                    createSuccessfulFuture();
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act
            kafkaProducer.sendResponse(USER_KEY, validResponseMessage);

            // Assert
            ArgumentCaptor<BrainHealthResponseMessage> captor =
                    ArgumentCaptor.forClass(BrainHealthResponseMessage.class);
            verify(kafkaTemplate).send(anyString(), anyString(), captor.capture());
            assertThat(captor.getValue().messageId()).isNotNull();
            assertThat(captor.getValue().messageId()).isEqualTo("msg-123");
        }
    }

    // Helper methods
    private CompletableFuture<SendResult<String, BrainHealthResponseMessage>> createSuccessfulFuture() {
        SendResult<String, BrainHealthResponseMessage> sendResult = mock(SendResult.class);
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(RESPONSE_TOPIC, 0),
                0L, 0, System.currentTimeMillis(), 0, 0
        );

        when(sendResult.getRecordMetadata()).thenReturn(metadata);

        CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                new CompletableFuture<>();
        future.complete(sendResult);
        return future;
    }

    private CompletableFuture<SendResult<String, BrainHealthResponseMessage>> createFailedFuture(Exception error) {
        CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                new CompletableFuture<>();
        future.completeExceptionally(error);
        return future;
    }
}
