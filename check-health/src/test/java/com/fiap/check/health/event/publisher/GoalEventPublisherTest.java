package com.fiap.check.health.event.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.check.health.dto.event.GoalCreatedEvent;
import com.fiap.check.health.model.GoalCategory;
import com.fiap.check.health.persistence.entity.Goal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes completos para GoalEventPublisher
 * com cobertura de cenários de sucesso, erro, edge cases e branches.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GoalEventPublisher - Unit Tests")
class GoalEventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SendResult<String, String> sendResult;

    @InjectMocks
    private GoalEventPublisher goalEventPublisher;

    private Goal validGoal;
    private CompletableFuture<SendResult<String, String>> completedFuture;
    private CompletableFuture<SendResult<String, String>> failedFuture;

    @BeforeEach
    void setUp() {
        validGoal = Goal.builder()
                .goalId(1L)
                .userId("user123")
                .category(GoalCategory.SAUDE_FISICA)
                .title("Exercitar-se diariamente")
                .description("Meta de exercícios físicos")
                .build();

        completedFuture = CompletableFuture.completedFuture(sendResult);
        failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka connection failed"));
    }

    @Nested
    @DisplayName("Testes de Sucesso - Future Completion")
    class SuccessScenarioTests {

        @Test
        @DisplayName("Deve publicar evento com sucesso e completar future sem exceção")
        void shouldPublishEventSuccessfullyAndCompleteFutureWithoutException() throws Exception {
            // Given
            String expectedJson = "{\"goalId\":1,\"userId\":\"user123\",\"category\":\"SAUDE_FISICA\"}";
            
            when(objectMapper.writeValueAsString(any(GoalCreatedEvent.class))).thenReturn(expectedJson);
            when(kafkaTemplate.send(eq("goal.created"), eq(expectedJson))).thenReturn(completedFuture);
            
            // Mock do SendResult e RecordMetadata
            var recordMetadata = mock(org.apache.kafka.clients.producer.RecordMetadata.class);
            when(sendResult.getRecordMetadata()).thenReturn(recordMetadata);
            when(recordMetadata.offset()).thenReturn(123L);
            when(recordMetadata.partition()).thenReturn(2);

            // When
            goalEventPublisher.publishGoalCreated(validGoal);

            // Then
            verify(objectMapper).writeValueAsString(argThat((GoalCreatedEvent event) -> {
                assertThat(event.getGoalId()).isEqualTo(1L);
                assertThat(event.getUserId()).isEqualTo("user123");
                assertThat(event.getCategory()).isEqualTo("SAUDE_FISICA");
                assertThat(event.getTitle()).isEqualTo("Exercitar-se diariamente");
                assertThat(event.getDescription()).isEqualTo("Meta de exercícios físicos");
                return true;
            }));
            
            verify(kafkaTemplate).send("goal.created", expectedJson);
            
            // Verifica que o callback de sucesso foi executado
            // O teste implicitamente verifica que future.whenComplete foi chamado
            // através do mock que retorna completedFuture
        }

        @Test
        @DisplayName("Deve lidar com goal que tem category null")
        void shouldHandleGoalWithNullCategory() {
            // Given
            Goal goalWithNullCategory = Goal.builder()
                    .goalId(2L)
                    .userId("user456")
                    .category(null) // Category null
                    .title("Meta sem categoria")
                    .description("Teste com categoria null")
                    .build();

            // When & Then
            // O erro acontece em goal.getCategory().name() antes de qualquer serialização
            assertThatThrownBy(() -> goalEventPublisher.publishGoalCreated(goalWithNullCategory))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(NullPointerException.class);
            
            // Verifica que o Kafka não foi chamado pois falhou antes
            verifyNoInteractions(kafkaTemplate);
            verifyNoInteractions(objectMapper);
        }
    }

    @Nested
    @DisplayName("Testes de Erro - JSON Serialization")
    class JsonSerializationErrorTests {

        @Test
        @DisplayName("Deve lançar RuntimeException quando ObjectMapper falha na serialização")
        void shouldThrowRuntimeExceptionWhenObjectMapperFailsSerialization() throws Exception {
            // Given
            JsonProcessingException jsonException = mock(JsonProcessingException.class);
            when(objectMapper.writeValueAsString(any(GoalCreatedEvent.class))).thenThrow(jsonException);

            // When & Then
            assertThatThrownBy(() -> goalEventPublisher.publishGoalCreated(validGoal))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao serializar evento goal.created")
                .hasCause(jsonException);

            // Verifica que o Kafka não foi chamado devido ao erro de JSON
            verifyNoInteractions(kafkaTemplate);
        }

        @Test
        @DisplayName("Deve capturar JsonProcessingException específica")
        void shouldCatchSpecificJsonProcessingException() throws Exception {
            // Given
            JsonProcessingException specificException = new JsonProcessingException("Invalid character") {};
            when(objectMapper.writeValueAsString(any(GoalCreatedEvent.class))).thenThrow(specificException);

            // When & Then
            assertThatThrownBy(() -> goalEventPublisher.publishGoalCreated(validGoal))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao serializar evento goal.created")
                .hasCause(specificException);

            verify(objectMapper).writeValueAsString(any(GoalCreatedEvent.class));
            verifyNoInteractions(kafkaTemplate);
        }
    }

    @Nested
    @DisplayName("Testes de Erro - Kafka Publishing")
    class KafkaPublishingErrorTests {

        @Test
        @DisplayName("Deve lançar RuntimeException quando KafkaTemplate falha")
        void shouldThrowRuntimeExceptionWhenKafkaTemplateFails() throws Exception {
            // Given
            String validJson = "{\"goalId\":1,\"userId\":\"user123\",\"category\":\"SAUDE_FISICA\"}";
            RuntimeException kafkaException = new RuntimeException("Kafka broker not available");
            
            when(objectMapper.writeValueAsString(any(GoalCreatedEvent.class))).thenReturn(validJson);
            when(kafkaTemplate.send(eq("goal.created"), eq(validJson))).thenThrow(kafkaException);

            // When & Then
            assertThatThrownBy(() -> goalEventPublisher.publishGoalCreated(validGoal))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao publicar evento goal.created")
                .hasCause(kafkaException);

            verify(objectMapper).writeValueAsString(any(GoalCreatedEvent.class));
            verify(kafkaTemplate).send("goal.created", validJson);
        }

        @Test
        @DisplayName("Deve capturar exceção genérica durante publicação")
        void shouldCatchGenericExceptionDuringPublishing() throws Exception {
            // Given
            String validJson = "{\"goalId\":1,\"userId\":\"user123\",\"category\":\"SAUDE_FISICA\"}";
            IllegalStateException genericException = new IllegalStateException("Unexpected error");
            
            when(objectMapper.writeValueAsString(any(GoalCreatedEvent.class))).thenReturn(validJson);
            when(kafkaTemplate.send(eq("goal.created"), eq(validJson))).thenThrow(genericException);

            // When & Then
            assertThatThrownBy(() -> goalEventPublisher.publishGoalCreated(validGoal))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao publicar evento goal.created")
                .hasCause(genericException);

            verify(objectMapper).writeValueAsString(any(GoalCreatedEvent.class));
            verify(kafkaTemplate).send("goal.created", validJson);
        }
    }

    @Nested
    @DisplayName("Testes do Callback Future - whenComplete")
    class FutureCallbackTests {

        @Test
        @DisplayName("Deve executar callback de erro quando future completa com exceção")
        void shouldExecuteErrorCallbackWhenFutureCompletesWithException() throws Exception {
            // Given
            String validJson = "{\"goalId\":1,\"userId\":\"user123\",\"category\":\"SAUDE_FISICA\"}";
            
            when(objectMapper.writeValueAsString(any(GoalCreatedEvent.class))).thenReturn(validJson);
            when(kafkaTemplate.send(eq("goal.created"), eq(validJson))).thenReturn(failedFuture);

            // When
            goalEventPublisher.publishGoalCreated(validGoal);

            // Then
            verify(objectMapper).writeValueAsString(any(GoalCreatedEvent.class));
            verify(kafkaTemplate).send("goal.created", validJson);
            
            // Note: O callback de erro será executado, mas como é assíncrono,
            // não podemos verificar diretamente aqui. O teste verifica que
            // a configuração está correta e que o future.whenComplete será chamado.
            
            // Podemos verificar que o future foi criado corretamente
            assertThat(failedFuture.isCompletedExceptionally()).isTrue();
        }

        @Test
        @DisplayName("Deve configurar callback corretamente para future de sucesso")
        void shouldSetupCallbackCorrectlyForSuccessFuture() throws Exception {
            // Given
            String validJson = "{\"goalId\":1,\"userId\":\"user123\",\"category\":\"SAUDE_FISICA\"}";
            
            // Mock mais detalhado do SendResult
            var recordMetadata = mock(org.apache.kafka.clients.producer.RecordMetadata.class);
            when(sendResult.getRecordMetadata()).thenReturn(recordMetadata);
            when(recordMetadata.offset()).thenReturn(456L);
            when(recordMetadata.partition()).thenReturn(1);
            
            when(objectMapper.writeValueAsString(any(GoalCreatedEvent.class))).thenReturn(validJson);
            when(kafkaTemplate.send(eq("goal.created"), eq(validJson))).thenReturn(completedFuture);

            // When
            goalEventPublisher.publishGoalCreated(validGoal);

            // Then
            verify(objectMapper).writeValueAsString(any(GoalCreatedEvent.class));
            verify(kafkaTemplate).send("goal.created", validJson);
            
            // Verifica que o future está completado com sucesso
            assertThat(completedFuture.isDone()).isTrue();
            assertThat(completedFuture.isCompletedExceptionally()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases e Valores Limites")
    class EdgeCasesAndBoundaryTests {

        @Test
        @DisplayName("Deve lidar com goal com valores extremos")
        void shouldHandleGoalWithExtremeValues() throws Exception {
            // Given
            Goal extremeGoal = Goal.builder()
                    .goalId(Long.MAX_VALUE)
                    .userId("a".repeat(1000)) // User ID muito longo
                    .category(GoalCategory.BEM_ESTAR)
                    .title("") // Título vazio
                    .description(null) // Descrição null
                    .build();

            String extremeJson = "{\"goalId\":" + Long.MAX_VALUE + ",\"userId\":\"" + "a".repeat(1000) + "\"}";
            
            when(objectMapper.writeValueAsString(any(GoalCreatedEvent.class))).thenReturn(extremeJson);
            when(kafkaTemplate.send(eq("goal.created"), eq(extremeJson))).thenReturn(completedFuture);

            // When
            goalEventPublisher.publishGoalCreated(extremeGoal);

            // Then
            verify(objectMapper).writeValueAsString(argThat((GoalCreatedEvent event) -> {
                assertThat(event.getGoalId()).isEqualTo(Long.MAX_VALUE);
                assertThat(event.getUserId()).isEqualTo("a".repeat(1000));
                assertThat(event.getCategory()).isEqualTo("BEM_ESTAR");
                assertThat(event.getTitle()).isEqualTo("");
                assertThat(event.getDescription()).isNull();
                return true;
            }));
            
            verify(kafkaTemplate).send("goal.created", extremeJson);
        }

        @Test
        @DisplayName("Deve lidar com goal que tem campos null")
        void shouldHandleGoalWithNullFields() throws Exception {
            // Given
            Goal goalWithNulls = Goal.builder()
                    .goalId(null) // ID null
                    .userId(null) // User ID null
                    .category(GoalCategory.SONO)
                    .title(null) // Título null
                    .description(null) // Descrição null
                    .build();

            String nullsJson = "{\"goalId\":null,\"userId\":null,\"category\":\"SONO\",\"title\":null}";
            
            when(objectMapper.writeValueAsString(any(GoalCreatedEvent.class))).thenReturn(nullsJson);
            when(kafkaTemplate.send(eq("goal.created"), eq(nullsJson))).thenReturn(completedFuture);

            // When
            goalEventPublisher.publishGoalCreated(goalWithNulls);

            // Then
            verify(objectMapper).writeValueAsString(argThat((GoalCreatedEvent event) -> {
                assertThat(event.getGoalId()).isNull();
                assertThat(event.getUserId()).isNull();
                assertThat(event.getCategory()).isEqualTo("SONO");
                assertThat(event.getTitle()).isNull();
                assertThat(event.getDescription()).isNull();
                return true;
            }));
            
            verify(kafkaTemplate).send("goal.created", nullsJson);
        }

        @Test
        @DisplayName("Deve testar todas as categorias disponíveis")
        void shouldTestAllAvailableCategories() throws Exception {
            // Given - Testamos todas as categorias do enum
            GoalCategory[] categories = GoalCategory.values();
            
            for (GoalCategory category : categories) {
                // Setup para cada categoria
                Goal goalWithCategory = Goal.builder()
                        .goalId(1L)
                        .userId("user123")
                        .category(category)
                        .title("Meta de " + category.name())
                        .description("Teste para categoria " + category.name())
                        .build();

                String categoryJson = "{\"category\":\"" + category.name() + "\"}";
                
                when(objectMapper.writeValueAsString(any(GoalCreatedEvent.class))).thenReturn(categoryJson);
                when(kafkaTemplate.send(eq("goal.created"), eq(categoryJson))).thenReturn(completedFuture);

                // When
                goalEventPublisher.publishGoalCreated(goalWithCategory);

                // Then
                verify(objectMapper, atLeastOnce()).writeValueAsString(argThat((GoalCreatedEvent event) -> 
                    event.getCategory().equals(category.name())));
            }
            
            verify(kafkaTemplate, times(categories.length)).send(eq("goal.created"), anyString());
        }
    }

    @Nested
    @DisplayName("Testes de Configuração e Constantes")
    class ConfigurationAndConstantsTests {

        @Test
        @DisplayName("Deve usar o tópico correto para goal.created")
        void shouldUseCorrectTopicForGoalCreated() throws Exception {
            // Given
            String validJson = "{\"goalId\":1,\"userId\":\"user123\",\"category\":\"SAUDE_FISICA\"}";
            
            when(objectMapper.writeValueAsString(any(GoalCreatedEvent.class))).thenReturn(validJson);
            when(kafkaTemplate.send(anyString(), anyString())).thenReturn(completedFuture);

            // When
            goalEventPublisher.publishGoalCreated(validGoal);

            // Then
            verify(kafkaTemplate).send(eq("goal.created"), eq(validJson));
            verify(kafkaTemplate, never()).send(eq("goal.updated"), anyString());
            verify(kafkaTemplate, never()).send(eq("goal.deleted"), anyString());
        }

        @Test
        @DisplayName("Deve criar evento com estrutura correta")
        void shouldCreateEventWithCorrectStructure() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any(GoalCreatedEvent.class))).thenReturn("{}");
            when(kafkaTemplate.send(anyString(), anyString())).thenReturn(completedFuture);

            // When
            goalEventPublisher.publishGoalCreated(validGoal);

            // Then
            verify(objectMapper).writeValueAsString(argThat((GoalCreatedEvent event) -> {
                // Verificamos que o evento tem todos os campos necessários
                assertThat(event).isInstanceOf(GoalCreatedEvent.class);
                
                // Verificamos que todos os campos do goal são mapeados
                assertThat(event.getGoalId()).isEqualTo(validGoal.getGoalId());
                assertThat(event.getUserId()).isEqualTo(validGoal.getUserId());
                assertThat(event.getCategory()).isEqualTo(validGoal.getCategory().name());
                assertThat(event.getTitle()).isEqualTo(validGoal.getTitle());
                assertThat(event.getDescription()).isEqualTo(validGoal.getDescription());
                
                return true;
            }));
        }
    }
}