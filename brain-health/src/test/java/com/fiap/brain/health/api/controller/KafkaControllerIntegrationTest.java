package com.fiap.brain.health.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.brain.health.api.dto.kafka.BrainHealthRequestMessage;
import com.fiap.brain.health.api.dto.request.KafkaTestMessageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("unchecked")
@WebMvcTest(KafkaController.class)
@TestPropertySource(properties = {
    "kafka.topic.consumer=brain-health-request",
    "kafka.topic.producer=brain-health-response"
})
@DisplayName("KafkaController - Testes de Integração")
class KafkaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, BrainHealthRequestMessage> requestKafkaTemplate;

    private KafkaTestMessageRequest validRequest;

    private static final String SEND_MESSAGE_PATH = "/api/v1/kafka/test/send";
    private static final String INFO_PATH = "/api/v1/kafka/info";
    private static final Long USER_ID = 12345L;
    private static final Long GOAL_ID = 1L;
    private static final String TITLE = "Benefícios da Caminhada";

    @BeforeEach
    void setUp() {
        validRequest = new KafkaTestMessageRequest(
            GOAL_ID,
            USER_ID,
            "SAUDE_FISICA",
            TITLE,
            "Artigo sobre os benefícios da caminhada"
        );
    }

    @Nested
    @DisplayName("1. POST /api/v1/kafka/test/send - Cenários de Sucesso")
    class SendMessageSuccessScenarios {

        @Test
        @DisplayName("Deve retornar 200 OK ao enviar mensagem para Kafka com sucesso")
        void shouldReturn200WhenSendingMessageSuccessfully() throws Exception {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));

            when(requestKafkaTemplate.send(anyString(), anyString(), any(BrainHealthRequestMessage.class)))
                .thenReturn(future);

            // Act & Assert
            mockMvc.perform(post(SEND_MESSAGE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value("SENT_TO_KAFKA"))
                    .andExpect(jsonPath("$.message").value("Message sent to Kafka topic: brain-health-request"))
                    .andExpect(jsonPath("$.messageId").exists())
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.goalId").value(GOAL_ID))
                    .andExpect(jsonPath("$.userId").value(USER_ID))
                    .andExpect(jsonPath("$.category").value("SAUDE_FISICA"))
                    .andExpect(jsonPath("$.title").value(TITLE))
                    .andExpect(jsonPath("$.description").value("Artigo sobre os benefícios da caminhada"))
                    .andExpect(jsonPath("$.topic").value("brain-health-request"))
                    .andExpect(jsonPath("$.note").exists());

            verify(requestKafkaTemplate, times(1)).send(eq("brain-health-request"), eq(String.valueOf(USER_ID)), any());
        }

        @Test
        @DisplayName("Deve gerar messageId e correlationId únicos")
        void shouldGenerateUniqueIds() throws Exception {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));

            when(requestKafkaTemplate.send(anyString(), anyString(), any(BrainHealthRequestMessage.class)))
                .thenReturn(future);

            // Act & Assert
            mockMvc.perform(post(SEND_MESSAGE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.messageId").isString())
                    .andExpect(jsonPath("$.messageId").isNotEmpty())
                    .andExpect(jsonPath("$.correlationId").isString())
                    .andExpect(jsonPath("$.correlationId").isNotEmpty());
        }

        @Test
        @DisplayName("Deve usar userId como key do Kafka")
        void shouldUseUserIdAsKafkaKey() throws Exception {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));

            when(requestKafkaTemplate.send(anyString(), anyString(), any(BrainHealthRequestMessage.class)))
                .thenReturn(future);

            // Act
            mockMvc.perform(post(SEND_MESSAGE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());

            // Assert
            verify(requestKafkaTemplate).send(
                eq("brain-health-request"),
                eq(String.valueOf(USER_ID)),
                any(BrainHealthRequestMessage.class)
            );
        }

        @Test
        @DisplayName("Deve incluir todos os campos do request na resposta")
        void shouldIncludeAllRequestFieldsInResponse() throws Exception {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));

            when(requestKafkaTemplate.send(anyString(), anyString(), any(BrainHealthRequestMessage.class)))
                .thenReturn(future);

            // Act & Assert
            mockMvc.perform(post(SEND_MESSAGE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.goalId").value(GOAL_ID))
                    .andExpect(jsonPath("$.userId").value(USER_ID))
                    .andExpect(jsonPath("$.category").value("SAUDE_FISICA"))
                    .andExpect(jsonPath("$.title").value(TITLE))
                    .andExpect(jsonPath("$.description").value("Artigo sobre os benefícios da caminhada"));
        }
    }

    @Nested
    @DisplayName("2. POST /api/v1/kafka/test/send - Validações de Request")
    class SendMessageValidationScenarios {

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando userId for null")
        void shouldReturn400WhenUserIdIsNull() throws Exception {
            // Arrange
            String invalidJson = """
                {
                    "goalId": 1,
                    "category": "SAUDE_FISICA",
                    "title": "Benefícios da Caminhada",
                    "description": "Artigo"
                }
                """;

            // Act & Assert
            mockMvc.perform(post(SEND_MESSAGE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(requestKafkaTemplate, never()).send(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando title for null")
        void shouldReturn400WhenTitleIsNull() throws Exception {
            // Arrange
            String invalidJson = """
                {
                    "goalId": 1,
                    "userId": 12345,
                    "category": "SAUDE_FISICA",
                    "description": "Artigo"
                }
                """;

            // Act & Assert
            mockMvc.perform(post(SEND_MESSAGE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());

            verify(requestKafkaTemplate, never()).send(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando title for vazio")
        void shouldReturn400WhenTitleIsBlank() throws Exception {
            // Arrange
            String invalidJson = """
                {
                    "goalId": 1,
                    "userId": 12345,
                    "category": "SAUDE_FISICA",
                    "title": "   ",
                    "description": "Artigo"
                }
                """;

            // Act & Assert
            mockMvc.perform(post(SEND_MESSAGE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());

            verify(requestKafkaTemplate, never()).send(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Deve aceitar request sem goalId (campo opcional)")
        void shouldAcceptRequestWithoutGoalId() throws Exception {
            // Arrange
            String validJson = """
                {
                    "userId": 12345,
                    "category": "SAUDE_FISICA",
                    "title": "Benefícios da Caminhada",
                    "description": "Artigo"
                }
                """;

            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));

            when(requestKafkaTemplate.send(anyString(), anyString(), any(BrainHealthRequestMessage.class)))
                .thenReturn(future);

            // Act & Assert
            mockMvc.perform(post(SEND_MESSAGE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve aceitar request sem category (campo opcional)")
        void shouldAcceptRequestWithoutCategory() throws Exception {
            // Arrange
            String validJson = """
                {
                    "goalId": 1,
                    "userId": 12345,
                    "title": "Benefícios da Caminhada",
                    "description": "Artigo"
                }
                """;

            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));

            when(requestKafkaTemplate.send(anyString(), anyString(), any(BrainHealthRequestMessage.class)))
                .thenReturn(future);

            // Act & Assert
            mockMvc.perform(post(SEND_MESSAGE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("3. GET /api/v1/kafka/info - Informações do Kafka")
    class GetKafkaInfoScenarios {

        @Test
        @DisplayName("Deve retornar 200 OK com informações do Kafka")
        void shouldReturn200WithKafkaInfo() throws Exception {
            // Act & Assert
            mockMvc.perform(get(INFO_PATH)
                    .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.requestTopic").value("brain-health-request"))
                    .andExpect(jsonPath("$.responseTopic").value("brain-health-response"))
                    .andExpect(jsonPath("$.status").value("Kafka integration active"));
        }

        @Test
        @DisplayName("Deve retornar informações corretas dos tópicos")
        void shouldReturnCorrectTopicInformation() throws Exception {
            // Act & Assert
            mockMvc.perform(get(INFO_PATH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.requestTopic").exists())
                    .andExpect(jsonPath("$.responseTopic").exists())
                    .andExpect(jsonPath("$.status").exists());
        }

        @Test
        @DisplayName("Deve aceitar requisição GET sem body")
        void shouldAcceptGetRequestWithoutBody() throws Exception {
            // Act & Assert
            mockMvc.perform(get(INFO_PATH))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("4. Validação de Headers e Content-Type")
    class HeaderValidationScenarios {

        @Test
        @DisplayName("Deve aceitar Content-Type application/json")
        void shouldAcceptApplicationJson() throws Exception {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));

            when(requestKafkaTemplate.send(anyString(), anyString(), any(BrainHealthRequestMessage.class)))
                .thenReturn(future);

            // Act & Assert
            mockMvc.perform(post(SEND_MESSAGE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Deve retornar 415 Unsupported Media Type quando Content-Type não é JSON")
        void shouldReturn415WhenContentTypeIsNotJson() throws Exception {
            // Act & Assert
            mockMvc.perform(post(SEND_MESSAGE_PATH)
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Nested
    @DisplayName("5. Testes de Múltiplas Requisições")
    class MultipleRequestsScenarios {

        @Test
        @DisplayName("Deve processar múltiplas mensagens independentemente")
        void shouldProcessMultipleMessagesIndependently() throws Exception {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));

            when(requestKafkaTemplate.send(anyString(), anyString(), any(BrainHealthRequestMessage.class)))
                .thenReturn(future);

            // Act & Assert - Multiple requests
            for (int i = 0; i < 3; i++) {
                mockMvc.perform(post(SEND_MESSAGE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("SENT_TO_KAFKA"));
            }

            verify(requestKafkaTemplate, times(3)).send(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Deve gerar IDs diferentes para cada mensagem")
        void shouldGenerateDifferentIdsForEachMessage() throws Exception {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));

            when(requestKafkaTemplate.send(anyString(), anyString(), any(BrainHealthRequestMessage.class)))
                .thenReturn(future);

            // Act - Send two messages
            String response1 = mockMvc.perform(post(SEND_MESSAGE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString(StandardCharsets.UTF_8);

            String response2 = mockMvc.perform(post(SEND_MESSAGE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString(StandardCharsets.UTF_8);

            // Assert - IDs should be different
            var json1 = objectMapper.readTree(response1);
            var json2 = objectMapper.readTree(response2);

            String messageId1 = json1.get("messageId").asText();
            String messageId2 = json2.get("messageId").asText();

            assertThat(messageId1).isNotEqualTo(messageId2);
        }
    }

    @Nested
    @DisplayName("6. Integração com Diferentes Payloads")
    class DifferentPayloadsScenarios {

        @Test
        @DisplayName("Deve processar mensagem com título diferente")
        void shouldProcessMessageWithDifferentTitle() throws Exception {
            // Arrange
            KafkaTestMessageRequest customRequest = new KafkaTestMessageRequest(
                2L,
                99999L,
                "SAUDE_MENTAL",
                "Meditação e Saúde Mental",
                "Artigo sobre meditação"
            );

            CompletableFuture future =
                CompletableFuture.completedFuture(mock(SendResult.class));

            when(requestKafkaTemplate.send(anyString(), anyString(), any(BrainHealthRequestMessage.class)))
                .thenReturn(future);

            // Act & Assert
            mockMvc.perform(post(SEND_MESSAGE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(customRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(99999L))
                    .andExpect(jsonPath("$.title").value("Meditação e Saúde Mental"))
                    .andExpect(jsonPath("$.category").value("SAUDE_MENTAL"));

            verify(requestKafkaTemplate).send(
                eq("brain-health-request"),
                eq("99999"),
                any(BrainHealthRequestMessage.class)
            );
        }

        @Test
        @DisplayName("Deve processar mensagem com campos mínimos obrigatórios")
        void shouldProcessMessageWithMinimalRequiredFields() throws Exception {
            // Arrange
            String minimalJson = """
                {
                    "userId": 12345,
                    "title": "Título Mínimo"
                }
                """;

            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));

            when(requestKafkaTemplate.send(anyString(), anyString(), any(BrainHealthRequestMessage.class)))
                .thenReturn(future);

            // Act & Assert
            mockMvc.perform(post(SEND_MESSAGE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(minimalJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(12345))
                    .andExpect(jsonPath("$.title").value("Título Mínimo"));
        }
    }
}
