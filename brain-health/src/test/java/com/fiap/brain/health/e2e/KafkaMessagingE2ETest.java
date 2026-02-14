package com.fiap.brain.health.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.brain.health.api.dto.kafka.BrainHealthRequestMessage;
import com.fiap.brain.health.api.dto.request.KafkaTestMessageRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes End-to-End (E2E) - Fluxo Completo Kafka
 * Simula uma aplicação externa fazendo requisições REST para enviar mensagens ao Kafka.
 * Fluxo completo testado:
 * 1. Cliente externo envia POST /api/v1/kafka/test/send
 * 2. Controller recebe e valida request
 * 3. Controller gera messageId e correlationId únicos
 * 4. Controller cria BrainHealthRequestMessage
 * 5. KafkaTemplate envia mensagem ao tópico brain-health-request-test
 * 6. Controller retorna confirmação HTTP com detalhes
 * Usa @SpringBootTest com servidor real (RANDOM_PORT)
 * Usa TestRestTemplate para simular cliente HTTP externo
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("E2E - Fluxo Completo Kafka Messaging")
class KafkaMessagingE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(name = "requestKafkaTemplate")
    @SuppressWarnings("rawtypes")
    private KafkaTemplate kafkaTemplate;

    private String baseUrl;
    private HttpHeaders headers;

    private static final Long USER_ID = 77777L;
    private static final Long GOAL_ID = 99L;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/kafka";

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    }

    @Nested
    @DisplayName("1. Fluxo de Sucesso - Envio de Mensagem ao Kafka")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SuccessFlowSendMessage {

        @Test
        @Order(1)
        @DisplayName("E2E-K001: Cliente envia mensagem de teste ao Kafka com sucesso")
        @SuppressWarnings("unchecked")
        void shouldSendTestMessageToKafkaSuccessfully() {
            // Arrange
            KafkaTestMessageRequest request = new KafkaTestMessageRequest(
                GOAL_ID,
                USER_ID,
                "SAUDE_FISICA",
                "Benefícios da Yoga",
                "Quero aprender sobre yoga para reduzir estresse"
            );

            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            HttpEntity<KafkaTestMessageRequest> httpRequest = new HttpEntity<>(request, headers);

            // Act - Cliente externo faz POST
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/test/send",
                HttpMethod.POST,
                httpRequest,
                Map.class
            );

            // Assert - Validar resposta HTTP
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

            Map<String, Object> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.get("status")).isEqualTo("SENT_TO_KAFKA");
            assertThat(body.get("message")).asString().contains("brain-health-request-test");
            assertThat(body.get("messageId")).isNotNull();
            assertThat(body.get("correlationId")).isNotNull();
            assertThat(body.get("goalId")).isEqualTo(GOAL_ID.intValue());
            assertThat(body.get("userId")).isEqualTo(USER_ID.intValue());
            assertThat(body.get("category")).isEqualTo("SAUDE_FISICA");
            assertThat(body.get("title")).isEqualTo("Benefícios da Yoga");
            assertThat(body.get("description")).isEqualTo("Quero aprender sobre yoga para reduzir estresse");
            assertThat(body.get("topic")).isEqualTo("brain-health-request-test");
            assertThat(body.get("note")).asString().contains("brain-health-response-test");

            // Verificar que Kafka foi chamado
            verify(kafkaTemplate, times(1)).send(
                eq("brain-health-request-test"),
                eq(String.valueOf(USER_ID)),
                any(BrainHealthRequestMessage.class)
            );
        }

        @Test
        @Order(2)
        @DisplayName("E2E-K002: messageId e correlationId são únicos em cada requisição")
        @SuppressWarnings("unchecked")
        void shouldGenerateUniqueIdsForEachRequest() {
            // Arrange
            KafkaTestMessageRequest request = new KafkaTestMessageRequest(
                1L, 12345L, "SAUDE_MENTAL", "Meditação", "Aprender meditação"
            );

            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            HttpEntity<KafkaTestMessageRequest> httpRequest = new HttpEntity<>(request, headers);

            // Act - Enviar duas requisições
            ResponseEntity<Map> response1 = restTemplate.exchange(
                baseUrl + "/test/send", HttpMethod.POST, httpRequest, Map.class
            );

            ResponseEntity<Map> response2 = restTemplate.exchange(
                baseUrl + "/test/send", HttpMethod.POST, httpRequest, Map.class
            );

            // Assert - IDs devem ser diferentes
            Map<String, Object> body1 = response1.getBody();
            Map<String, Object> body2 = response2.getBody();

            assertThat(body1).isNotNull();
            assertThat(body2).isNotNull();

            String messageId1 = (String) body1.get("messageId");
            String messageId2 = (String) body2.get("messageId");
            String correlationId1 = (String) body1.get("correlationId");
            String correlationId2 = (String) body2.get("correlationId");

            assertThat(messageId1).isNotEqualTo(messageId2);
            assertThat(correlationId1).isNotEqualTo(correlationId2);
            assertThat(messageId1).isNotEmpty();
            assertThat(messageId2).isNotEmpty();
        }

        @Test
        @Order(3)
        @DisplayName("E2E-K003: userId é usado como key da mensagem Kafka")
        @SuppressWarnings("unchecked")
        void shouldUseUserIdAsKafkaKey() {
            // Arrange
            Long customUserId = 99999L;
            KafkaTestMessageRequest request = new KafkaTestMessageRequest(
                1L, customUserId, "SAUDE_NUTRICAO", "Dieta Mediterrânea", "Descrição"
            );

            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            HttpEntity<KafkaTestMessageRequest> httpRequest = new HttpEntity<>(request, headers);

            // Act
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/test/send", HttpMethod.POST, httpRequest, Map.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Verificar que a key do Kafka é o userId
            verify(kafkaTemplate).send(
                eq("brain-health-request-test"),
                eq(String.valueOf(customUserId)),
                any(BrainHealthRequestMessage.class)
            );
        }

        @Test
        @Order(4)
        @DisplayName("E2E-K004: Validar estrutura JSON completa da resposta")
        void shouldReturnCompleteJsonStructure() throws Exception {
            // Arrange
            KafkaTestMessageRequest request = new KafkaTestMessageRequest(
                GOAL_ID, USER_ID, "SAUDE_FISICA", "Pilates", "Benefícios do pilates"
            );

            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            HttpEntity<KafkaTestMessageRequest> httpRequest = new HttpEntity<>(request, headers);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/test/send", HttpMethod.POST, httpRequest, String.class
            );

            // Assert - Validar JSON raw
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            String jsonBody = response.getBody();
            assertThat(jsonBody).isNotNull();
            assertThat(jsonBody).contains("\"status\":");
            assertThat(jsonBody).contains("\"message\":");
            assertThat(jsonBody).contains("\"messageId\":");
            assertThat(jsonBody).contains("\"correlationId\":");
            assertThat(jsonBody).contains("\"goalId\":");
            assertThat(jsonBody).contains("\"userId\":");
            assertThat(jsonBody).contains("\"category\":");
            assertThat(jsonBody).contains("\"title\":");
            assertThat(jsonBody).contains("\"description\":");
            assertThat(jsonBody).contains("\"topic\":");
            assertThat(jsonBody).contains("\"note\":");

            // Deserializar e validar
            Map<String, Object> body = objectMapper.readValue(jsonBody, Map.class);
            assertThat(body).containsKeys(
                "status", "message", "messageId", "correlationId",
                "goalId", "userId", "category", "title", "description", "topic", "note"
            );
        }
    }

    @Nested
    @DisplayName("2. Fluxo de Erro - Validações de Request")
    class ErrorFlowValidations {

        @Test
        @Order(5)
        @DisplayName("E2E-K005: Cliente envia request sem userId e recebe 400 Bad Request")
        void shouldReturn400WhenUserIdIsMissing() {
            // Arrange
            String invalidJson = """
                {
                    "goalId": 1,
                    "category": "SAUDE_FISICA",
                    "title": "Teste",
                    "description": "Descrição"
                }
                """;

            HttpEntity<String> httpRequest = new HttpEntity<>(invalidJson, headers);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/test/send", HttpMethod.POST, httpRequest, String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
        }

        @Test
        @Order(6)
        @DisplayName("E2E-K006: Cliente envia request sem title e recebe 400 Bad Request")
        void shouldReturn400WhenTitleIsMissing() {
            // Arrange
            String invalidJson = """
                {
                    "goalId": 1,
                    "userId": 12345,
                    "category": "SAUDE_FISICA",
                    "description": "Descrição"
                }
                """;

            HttpEntity<String> httpRequest = new HttpEntity<>(invalidJson, headers);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/test/send", HttpMethod.POST, httpRequest, String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
        }

        @Test
        @Order(7)
        @DisplayName("E2E-K007: Cliente envia title vazio e recebe 400 Bad Request")
        void shouldReturn400WhenTitleIsBlank() {
            // Arrange
            String invalidJson = """
                {
                    "goalId": 1,
                    "userId": 12345,
                    "category": "SAUDE_FISICA",
                    "title": "   ",
                    "description": "Descrição"
                }
                """;

            HttpEntity<String> httpRequest = new HttpEntity<>(invalidJson, headers);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/test/send", HttpMethod.POST, httpRequest, String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @Order(8)
        @DisplayName("E2E-K008: Cliente envia JSON malformado e recebe 400 Bad Request")
        void shouldReturn400WhenJsonIsMalformed() {
            // Arrange
            String malformedJson = "{ invalid json }";
            HttpEntity<String> httpRequest = new HttpEntity<>(malformedJson, headers);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/test/send", HttpMethod.POST, httpRequest, String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("3. Campos Opcionais - Flexibilidade do Request")
    class OptionalFieldsTests {

        @Test
        @Order(9)
        @DisplayName("E2E-K009: Aceitar request sem goalId (campo opcional)")
        @SuppressWarnings("unchecked")
        void shouldAcceptRequestWithoutGoalId() {
            // Arrange
            String validJson = """
                {
                    "userId": 12345,
                    "category": "SAUDE_FISICA",
                    "title": "Corrida",
                    "description": "Descrição"
                }
                """;

            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            HttpEntity<String> httpRequest = new HttpEntity<>(validJson, headers);

            // Act
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/test/send", HttpMethod.POST, httpRequest, Map.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().get("goalId")).isNull();
            verify(kafkaTemplate).send(anyString(), anyString(), any());
        }

        @Test
        @Order(10)
        @DisplayName("E2E-K010: Aceitar request sem category (campo opcional)")
        @SuppressWarnings("unchecked")
        void shouldAcceptRequestWithoutCategory() {
            // Arrange
            String validJson = """
                {
                    "goalId": 1,
                    "userId": 12345,
                    "title": "Natação",
                    "description": "Descrição"
                }
                """;

            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            HttpEntity<String> httpRequest = new HttpEntity<>(validJson, headers);

            // Act
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/test/send", HttpMethod.POST, httpRequest, Map.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().get("category")).isNull();
            verify(kafkaTemplate).send(anyString(), anyString(), any());
        }

        @Test
        @Order(11)
        @DisplayName("E2E-K011: Aceitar request com campos mínimos obrigatórios")
        @SuppressWarnings("unchecked")
        void shouldAcceptRequestWithMinimalFields() {
            // Arrange - Apenas userId e title
            String minimalJson = """
                {
                    "userId": 12345,
                    "title": "Título Mínimo"
                }
                """;

            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            HttpEntity<String> httpRequest = new HttpEntity<>(minimalJson, headers);

            // Act
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/test/send", HttpMethod.POST, httpRequest, Map.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            Map<String, Object> body = response.getBody();
            assertThat(body.get("userId")).isEqualTo(12345);
            assertThat(body.get("title")).isEqualTo("Título Mínimo");
            assertThat(body.get("goalId")).isNull();
            assertThat(body.get("category")).isNull();
            assertThat(body.get("description")).isNull();
        }
    }

    @Nested
    @DisplayName("4. GET /api/v1/kafka/info - Informações do Kafka")
    class KafkaInfoEndpoint {

        @Test
        @Order(12)
        @DisplayName("E2E-K012: Cliente consulta informações do Kafka")
        void shouldReturnKafkaInformation() {
            // Act
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/info",
                HttpMethod.GET,
                null,
                Map.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            Map<String, Object> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.get("requestTopic")).isEqualTo("brain-health-request-test");
            assertThat(body.get("responseTopic")).isEqualTo("brain-health-response-test");
            assertThat(body.get("status")).isEqualTo("Kafka integration active");
        }

        @Test
        @Order(13)
        @DisplayName("E2E-K013: GET /info retorna headers corretos")
        void shouldReturnCorrectHeadersForInfo() {
            // Act
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/info",
                HttpMethod.GET,
                null,
                Map.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        }
    }

    @Nested
    @DisplayName("5. Múltiplas Requisições e Isolamento")
    class MultipleRequestsTests {

        @Test
        @Order(14)
        @DisplayName("E2E-K014: Processar múltiplas mensagens de diferentes clientes")
        @SuppressWarnings("unchecked")
        void shouldProcessMultipleMessagesFromDifferentClients() {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act - 3 clientes diferentes
            for (int i = 1; i <= 3; i++) {
                KafkaTestMessageRequest request = new KafkaTestMessageRequest(
                    (long) i,
                    (long) (1000 + i),
                    "CATEGORY_" + i,
                    "Title " + i,
                    "Description " + i
                );

                HttpEntity<KafkaTestMessageRequest> httpRequest = new HttpEntity<>(request, headers);

                ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/test/send", HttpMethod.POST, httpRequest, Map.class
                );

                // Assert individual
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = response.getBody();
                assertThat(body.get("userId")).isEqualTo(1000 + i);
                assertThat(body.get("title")).isEqualTo("Title " + i);
            }

            // Assert global - 3 mensagens enviadas
            verify(kafkaTemplate, times(3)).send(anyString(), anyString(), any());
        }

        @Test
        @Order(15)
        @DisplayName("E2E-K015: Cada mensagem tem key única baseada no userId")
        @SuppressWarnings("unchecked")
        void shouldUseUniqueKeyForEachMessage() {
            // Arrange
            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // Act - 3 mensagens com userIds diferentes
            Long[] userIds = {111L, 222L, 333L};

            for (Long userId : userIds) {
                KafkaTestMessageRequest request = new KafkaTestMessageRequest(
                    1L, userId, "SAUDE_FISICA", "Title", "Description"
                );

                HttpEntity<KafkaTestMessageRequest> httpRequest = new HttpEntity<>(request, headers);
                restTemplate.exchange(baseUrl + "/test/send", HttpMethod.POST, httpRequest, Map.class);
            }

            // Assert - Verificar keys diferentes
            verify(kafkaTemplate).send(eq("brain-health-request-test"), eq("111"), any());
            verify(kafkaTemplate).send(eq("brain-health-request-test"), eq("222"), any());
            verify(kafkaTemplate).send(eq("brain-health-request-test"), eq("333"), any());
        }
    }

    @Nested
    @DisplayName("6. Content Negotiation e Headers")
    class ContentNegotiationTests {

        @Test
        @Order(16)
        @DisplayName("E2E-K016: Cliente envia Content-Type inválido e recebe 415")
        void shouldReturn415WhenContentTypeIsInvalid() {
            // Arrange
            HttpHeaders invalidHeaders = new HttpHeaders();
            invalidHeaders.setContentType(MediaType.TEXT_PLAIN);

            // Enviar como String para simular Content-Type inválido
            String requestBody = "Plain text message, not JSON";

            HttpEntity<String> httpRequest = new HttpEntity<>(requestBody, invalidHeaders);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/test/send", HttpMethod.POST, httpRequest, String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        @Test
        @Order(17)
        @DisplayName("E2E-K017: Response contém Content-Type application/json")
        @SuppressWarnings("unchecked")
        void shouldReturnJsonContentType() {
            // Arrange
            KafkaTestMessageRequest request = new KafkaTestMessageRequest(
                1L, USER_ID, "SAUDE_FISICA", "Test", "Description"
            );

            CompletableFuture<SendResult<String, BrainHealthRequestMessage>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            HttpEntity<KafkaTestMessageRequest> httpRequest = new HttpEntity<>(request, headers);

            // Act
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/test/send", HttpMethod.POST, httpRequest, Map.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        }
    }

    @AfterEach
    void cleanup() {
        reset(kafkaTemplate);
    }
}
