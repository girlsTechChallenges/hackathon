package com.fiap.brain.health.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.brain.health.api.dto.request.AIArticleRequest;
import com.fiap.brain.health.api.dto.response.ArticleResponse;
import com.fiap.brain.health.domain.model.MedicalArticle;
import com.fiap.brain.health.domain.port.AIProcessingPort;
import com.fiap.brain.health.domain.port.MedicalArticleRepositoryPort;
import com.fiap.brain.health.infrastructure.adapter.kafka.BrainHealthKafkaProducer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("E2E - Fluxo Completo de Busca de Artigo Médico")
class ArticleSearchE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MedicalArticleRepositoryPort articleRepository;

    @MockBean
    private AIProcessingPort aiProcessing;

    @MockBean
    private BrainHealthKafkaProducer kafkaProducer;

    private String baseUrl;
    private HttpHeaders headers;

    private static final Long USER_ID = 98765L;
    private static final Long GOAL_ID = 42L;
    private static final String CATEGORY = "SAUDE_FISICA";

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/ai/articles";

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    }

    @Nested
    @DisplayName("1. Fluxo de Sucesso Completo - Caminhada")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SuccessFlowWalkingArticle {

        private MedicalArticle walkingArticle;
        private AIProcessingPort.AIProcessingResult walkingAIResult;

        @BeforeEach
        void setupWalkingScenario() {
            // Simula artigo real do CREMESP sobre caminhada
            walkingArticle = new MedicalArticle(
                "A caminhada é uma das formas mais acessíveis e eficazes de atividade física. " +
                "Estudos científicos comprovam que caminhar regularmente, por pelo menos 30 minutos diários, " +
                "traz inúmeros benefícios para a saúde cardiovascular, mental e metabólica. " +
                "A prática reduz o risco de doenças crônicas como diabetes tipo 2, hipertensão e obesidade. " +
                "Além disso, fortalece os músculos, melhora o equilíbrio e aumenta a densidade óssea, " +
                "prevenindo osteoporose. Para idosos, a caminhada regular está associada à redução do " +
                "declínio cognitivo e melhora da qualidade do sono.",
                "https://cremesp.org.br/artigos/beneficios-caminhada-saude"
            );

            // Simula processamento de IA realista
            walkingAIResult = new AIProcessingPort.AIProcessingResult(
                "Benefícios da Caminhada para a Saúde Integral",
                "A caminhada é uma atividade física democrática e extremamente benéfica. " +
                "Acessível a pessoas de todas as idades e condições físicas, " +
                "representa uma das formas mais eficazes de promoção da saúde.",
                List.of(
                    new AIProcessingPort.RecommendationItem(
                        "Frequência e Duração",
                        "Para obter benefícios significativos, caminhe pelo menos 30 minutos por dia, 5 dias por semana",
                        List.of(
                            "Inicie com 10-15 minutos e aumente gradualmente",
                            "Divida em sessões de 10 minutos se necessário",
                            "Mantenha consistência acima de intensidade inicial"
                        )
                    ),
                    new AIProcessingPort.RecommendationItem(
                        "Intensidade Adequada",
                        "Mantenha ritmo moderado onde você consegue conversar mas não cantar",
                        List.of(
                            "Frequência cardíaca entre 50-70% da máxima",
                            "Postura ereta com olhar no horizonte",
                            "Braços flexionados em 90 graus"
                        )
                    ),
                    new AIProcessingPort.RecommendationItem(
                        "Equipamento e Segurança",
                        "Use calçados apropriados e caminhe em locais seguros",
                        List.of(
                            "Tênis com bom amortecimento e suporte",
                            "Roupas leves e respiráveis",
                            "Evite horários de sol intenso (10h-16h)",
                            "Mantenha-se hidratado antes, durante e depois"
                        )
                    )
                ),
                "A caminhada regular é um investimento valioso na sua saúde. " +
                "Comece hoje mesmo e colha benefícios físicos, mentais e sociais ao longo da vida.",
                List.of(
                    new AIProcessingPort.QuizItem(
                        "Qual a duração mínima recomendada de caminhada por dia?",
                        List.of("10 minutos", "20 minutos", "30 minutos", "60 minutos"),
                        "30 minutos"
                    ),
                    new AIProcessingPort.QuizItem(
                        "Em qual faixa deve estar a frequência cardíaca durante a caminhada moderada?",
                        List.of("30-40% da máxima", "50-70% da máxima", "80-90% da máxima", "Acima de 90%"),
                        "50-70% da máxima"
                    ),
                    new AIProcessingPort.QuizItem(
                        "Qual o melhor horário para caminhar evitando sol intenso?",
                        List.of("Antes das 10h ou após 16h", "Entre 10h e 14h", "Apenas à noite", "Não faz diferença"),
                        "Antes das 10h ou após 16h"
                    )
                ),
                LocalDateTime.of(2026, 2, 11, 15, 30)
            );
        }

        @Test
        @Order(1)
        @DisplayName("E2E-001: Cliente solicita artigo sobre caminhada e recebe resposta completa")
        void shouldCompleteFullFlowForWalkingArticle() {
            // Arrange - Preparar cenário
            AIArticleRequest request = new AIArticleRequest(
                GOAL_ID,
                USER_ID,
                CATEGORY,
                "Benefícios da Caminhada",
                "Quero aprender sobre os benefícios da caminhada para minha saúde"
            );

            when(articleRepository.findByTopic("Benefícios da Caminhada"))
                .thenReturn(Optional.of(walkingArticle));
            when(aiProcessing.processArticle(eq("Benefícios da Caminhada"), any()))
                .thenReturn(walkingAIResult);
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            HttpEntity<AIArticleRequest> httpRequest = new HttpEntity<>(request, headers);

            // Act - Executar requisição REST como cliente externo
            ResponseEntity<ArticleResponse> response = restTemplate.exchange(
                baseUrl + "/search",
                HttpMethod.POST,
                httpRequest,
                ArticleResponse.class
            );

            // Assert - Validar resposta completa
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

            ArticleResponse article = response.getBody();
            assertThat(article).isNotNull();
            assertThat(article.title()).isEqualTo("Benefícios da Caminhada para a Saúde Integral");
            assertThat(article.introduction()).contains("democrática", "benéfica");
            assertThat(article.conclusion()).contains("investimento valioso");
            assertThat(article.sourceLink()).isEqualTo("https://cremesp.org.br/artigos/beneficios-caminhada-saude");
            assertThat(article.timestamp()).isNotNull();

            // Validar recommendations
            assertThat(article.recommendations()).hasSize(3);
            assertThat(article.recommendations().get(0).category()).isEqualTo("Frequência e Duração");
            assertThat(article.recommendations().get(0).tips()).hasSize(3);
            assertThat(article.recommendations().get(1).category()).isEqualTo("Intensidade Adequada");
            assertThat(article.recommendations().get(2).category()).isEqualTo("Equipamento e Segurança");
            assertThat(article.recommendations().get(2).tips()).hasSize(4);

            // Validar quizzes
            assertThat(article.quizzes()).hasSize(3);
            assertThat(article.quizzes().get(0).correctAnswer()).isEqualTo("30 minutos");
            assertThat(article.quizzes().get(1).correctAnswer()).isEqualTo("50-70% da máxima");
            assertThat(article.quizzes().get(2).correctAnswer()).isEqualTo("Antes das 10h ou após 16h");

            // Verificar que toda stack foi executada
            verify(articleRepository, times(2)).findByTopic("Benefícios da Caminhada");
            verify(aiProcessing).processArticle(eq("Benefícios da Caminhada"), any());
            verify(kafkaProducer).sendResponse(eq(String.valueOf(USER_ID)), any());
        }

        @Test
        @Order(2)
        @DisplayName("E2E-002: Validar estrutura JSON completa da resposta")
        void shouldReturnCompleteJsonStructure() throws Exception {
            // Arrange
            AIArticleRequest request = new AIArticleRequest(
                GOAL_ID, USER_ID, CATEGORY, "Benefícios da Caminhada", null
            );

            when(articleRepository.findByTopic("Benefícios da Caminhada"))
                .thenReturn(Optional.of(walkingArticle));
            when(aiProcessing.processArticle(anyString(), any()))
                .thenReturn(walkingAIResult);
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            HttpEntity<AIArticleRequest> httpRequest = new HttpEntity<>(request, headers);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/search",
                HttpMethod.POST,
                httpRequest,
                String.class
            );

            // Assert - Validar JSON raw
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            String jsonBody = response.getBody();
            assertThat(jsonBody).isNotNull();
            assertThat(jsonBody).contains("\"title\":");
            assertThat(jsonBody).contains("\"introduction\":");
            assertThat(jsonBody).contains("\"recommendations\":");
            assertThat(jsonBody).contains("\"category\":");
            assertThat(jsonBody).contains("\"description\":");
            assertThat(jsonBody).contains("\"tips\":");
            assertThat(jsonBody).contains("\"quizzes\":");
            assertThat(jsonBody).contains("\"question\":");
            assertThat(jsonBody).contains("\"options\":");
            assertThat(jsonBody).contains("\"correctAnswer\":");
            assertThat(jsonBody).contains("\"sourceLink\":");
            assertThat(jsonBody).contains("\"timestamp\":");

            // Deserializar e validar tipos
            ArticleResponse article = objectMapper.readValue(jsonBody, ArticleResponse.class);
            assertThat(article.recommendations()).allMatch(rec ->
                rec.category() != null && rec.description() != null && rec.tips() != null
            );
            assertThat(article.quizzes()).allMatch(quiz ->
                quiz.question() != null && quiz.options() != null && quiz.correctAnswer() != null
            );
        }
    }

    @Nested
    @DisplayName("2. Fluxo de Sucesso Completo - Diabetes")
    class SuccessFlowDiabetesArticle {

        @Test
        @Order(3)
        @DisplayName("E2E-003: Cliente solicita artigo sobre diabetes e alimentação")
        void shouldCompleteFullFlowForDiabetesArticle() {
            // Arrange - Cenário diferente: Diabetes
            MedicalArticle diabetesArticle = new MedicalArticle(
                "O controle glicêmico adequado é fundamental para pacientes com diabetes mellitus tipo 2. " +
                "A alimentação desempenha papel crucial nesse controle. Estudos demonstram que uma dieta " +
                "balanceada rica em fibras, com baixo índice glicêmico e controle de carboidratos, " +
                "pode reduzir significativamente os níveis de hemoglobina glicada (HbA1c). " +
                "Alimentos integrais, vegetais não amiláceos e proteínas magras devem compor a base alimentar.",
                "https://scielo.br/diabetes-alimentacao-controle"
            );

            AIProcessingPort.AIProcessingResult diabetesAIResult = new AIProcessingPort.AIProcessingResult(
                "Diabetes e Alimentação: Guia Prático",
                "A alimentação adequada é um dos pilares do controle do diabetes.",
                List.of(
                    new AIProcessingPort.RecommendationItem(
                        "Controle de Carboidratos",
                        "Monitore a quantidade e qualidade dos carboidratos consumidos",
                        List.of("Prefira carboidratos complexos", "Evite açúcares simples", "Use método do prato")
                    )
                ),
                "Uma alimentação equilibrada é essencial para o controle glicêmico.",
                List.of(
                    new AIProcessingPort.QuizItem(
                        "Qual tipo de carboidrato é mais indicado?",
                        List.of("Simples", "Complexos", "Refinados", "Processados"),
                        "Complexos"
                    )
                ),
                LocalDateTime.now()
            );

            AIArticleRequest request = new AIArticleRequest(
                2L, 55555L, "SAUDE_NUTRICAO",
                "Diabetes e Alimentação",
                "Preciso entender como me alimentar com diabetes"
            );

            when(articleRepository.findByTopic("Diabetes e Alimentação"))
                .thenReturn(Optional.of(diabetesArticle));
            when(aiProcessing.processArticle(eq("Diabetes e Alimentação"), any()))
                .thenReturn(diabetesAIResult);
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            HttpEntity<AIArticleRequest> httpRequest = new HttpEntity<>(request, headers);

            // Act
            ResponseEntity<ArticleResponse> response = restTemplate.exchange(
                baseUrl + "/search",
                HttpMethod.POST,
                httpRequest,
                ArticleResponse.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArticleResponse article = response.getBody();

            assertThat(article).isNotNull();
            assertThat(article.title()).isEqualTo("Diabetes e Alimentação: Guia Prático");
            assertThat(article.sourceLink()).contains("scielo.br");
            assertThat(article.recommendations()).hasSize(1);
            assertThat(article.recommendations().getFirst().category()).isEqualTo("Controle de Carboidratos");

            verify(kafkaProducer).sendResponse(eq("55555"), any());
        }
    }

    @Nested
    @DisplayName("3. Fluxos de Erro - Validações")
    class ErrorFlowValidations {

        @Test
        @Order(4)
        @DisplayName("E2E-004: Cliente envia request sem userId e recebe 400 Bad Request")
        void shouldReturn400WhenUserIdIsMissing() {
            // Arrange - Request inválido sem userId
            String invalidJson = """
                {
                    "goalId": 1,
                    "category": "SAUDE_FISICA",
                    "title": "Teste",
                    "description": "Descrição"
                }
                """;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpRequest = new HttpEntity<>(invalidJson, headers);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/search",
                HttpMethod.POST,
                httpRequest,
                String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // Não deve chamar nenhum serviço
            verify(articleRepository, never()).findByTopic(any());
            verify(aiProcessing, never()).processArticle(any(), any());
            verify(kafkaProducer, never()).sendResponse(any(), any());
        }

        @Test
        @Order(5)
        @DisplayName("E2E-005: Cliente envia request sem title e recebe 400 Bad Request")
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
                baseUrl + "/search",
                HttpMethod.POST,
                httpRequest,
                String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            verify(articleRepository, never()).findByTopic(any());
        }

        @Test
        @Order(6)
        @DisplayName("E2E-006: Cliente envia JSON malformado e recebe 400 Bad Request")
        void shouldReturn400WhenJsonIsMalformed() {
            // Arrange - JSON inválido
            String malformedJson = "{ invalid json here }";
            HttpEntity<String> httpRequest = new HttpEntity<>(malformedJson, headers);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/search",
                HttpMethod.POST,
                httpRequest,
                String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("4. Fluxos de Erro - Regras de Negócio")
    class ErrorFlowBusinessRules {

        @Test
        @Order(7)
        @DisplayName("E2E-007: Artigo não encontrado retorna 404 Not Found")
        void shouldReturn404WhenArticleNotFound() {
            // Arrange
            AIArticleRequest request = new AIArticleRequest(
                1L, USER_ID, CATEGORY, "Artigo Inexistente", null
            );

            when(articleRepository.findByTopic("Artigo Inexistente"))
                .thenReturn(Optional.empty());

            HttpEntity<AIArticleRequest> httpRequest = new HttpEntity<>(request, headers);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/search",
                HttpMethod.POST,
                httpRequest,
                String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

            verify(articleRepository).findByTopic("Artigo Inexistente");
            verify(aiProcessing, never()).processArticle(any(), any());
            verify(kafkaProducer, never()).sendResponse(any(), any());
        }

        @Test
        @Order(8)
        @DisplayName("E2E-008: Artigo com conteúdo insuficiente retorna 422 Unprocessable Entity")
        void shouldReturn422WhenContentInsufficient() {
            // Arrange - Artigo com menos de 100 caracteres
            MedicalArticle shortArticle = new MedicalArticle(
                "Conteúdo muito curto",  // Apenas 20 caracteres
                "https://example.com/short"
            );

            AIArticleRequest request = new AIArticleRequest(
                1L, USER_ID, CATEGORY, "Artigo Curto", null
            );

            when(articleRepository.findByTopic("Artigo Curto"))
                .thenReturn(Optional.of(shortArticle));

            HttpEntity<AIArticleRequest> httpRequest = new HttpEntity<>(request, headers);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/search",
                HttpMethod.POST,
                httpRequest,
                String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

            verify(articleRepository).findByTopic("Artigo Curto");
            verify(aiProcessing, never()).processArticle(any(), any());
            verify(kafkaProducer, never()).sendResponse(any(), any());
        }

        @Test
        @Order(9)
        @DisplayName("E2E-009: Erro no processamento de IA retorna 500 Internal Server Error")
        void shouldReturn500WhenAIProcessingFails() {
            // Arrange
            MedicalArticle validArticle = new MedicalArticle("A".repeat(200), "https://example.com");

            AIArticleRequest request = new AIArticleRequest(
                1L, USER_ID, CATEGORY, "Artigo com Erro IA", null
            );

            when(articleRepository.findByTopic("Artigo com Erro IA"))
                .thenReturn(Optional.of(validArticle));
            when(aiProcessing.processArticle(eq("Artigo com Erro IA"), any()))
                .thenThrow(new AIProcessingPort.AIProcessingException("OpenAI API timeout"));

            HttpEntity<AIArticleRequest> httpRequest = new HttpEntity<>(request, headers);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/search",
                HttpMethod.POST,
                httpRequest,
                String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

            verify(articleRepository).findByTopic("Artigo com Erro IA");
            verify(aiProcessing).processArticle(eq("Artigo com Erro IA"), any());
            verify(kafkaProducer, never()).sendResponse(any(), any());
        }
    }

    @Nested
    @DisplayName("5. Testes de Headers e Content Negotiation")
    class HeadersAndContentNegotiation {

        @Test
        @Order(10)
        @DisplayName("E2E-010: Cliente envia Content-Type inválido e recebe 415 Unsupported Media Type")
        void shouldReturn415WhenContentTypeIsInvalid() {
            // Arrange
            HttpHeaders invalidHeaders = new HttpHeaders();
            invalidHeaders.setContentType(MediaType.TEXT_PLAIN);

            // Enviar como String para simular Content-Type inválido
            String requestBody = "This is plain text content, not JSON";

            HttpEntity<String> httpRequest = new HttpEntity<>(requestBody, invalidHeaders);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/search",
                HttpMethod.POST,
                httpRequest,
                String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        @Test
        @Order(11)
        @DisplayName("E2E-011: Response contém headers corretos")
        void shouldReturnCorrectResponseHeaders() {
            // Arrange
            MedicalArticle article = new MedicalArticle("A".repeat(150), "https://example.com");
            AIProcessingPort.AIProcessingResult aiResult = new AIProcessingPort.AIProcessingResult(
                "Title", "Intro", List.of(), "Conclusion", List.of(), LocalDateTime.now()
            );

            AIArticleRequest request = new AIArticleRequest(
                1L, USER_ID, CATEGORY, "Test Headers", null
            );

            when(articleRepository.findByTopic("Test Headers")).thenReturn(Optional.of(article));
            when(aiProcessing.processArticle(anyString(), any())).thenReturn(aiResult);
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            HttpEntity<AIArticleRequest> httpRequest = new HttpEntity<>(request, headers);

            // Act
            ResponseEntity<ArticleResponse> response = restTemplate.exchange(
                baseUrl + "/search",
                HttpMethod.POST,
                httpRequest,
                ArticleResponse.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
            assertThat(response.getHeaders().get("Content-Type")).contains("application/json");
        }
    }

    @Nested
    @DisplayName("6. Testes de Performance e Múltiplas Requisições")
    class PerformanceTests {

        @Test
        @Order(12)
        @DisplayName("E2E-012: Múltiplos clientes fazem requisições simultâneas")
        void shouldHandleMultipleSimultaneousRequests() {
            // Arrange
            MedicalArticle article = new MedicalArticle("A".repeat(200), "https://example.com");
            AIProcessingPort.AIProcessingResult aiResult = new AIProcessingPort.AIProcessingResult(
                "Title", "Intro", List.of(), "Conclusion", List.of(), LocalDateTime.now()
            );

            when(articleRepository.findByTopic(anyString())).thenReturn(Optional.of(article));
            when(aiProcessing.processArticle(anyString(), any())).thenReturn(aiResult);
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act - Simular 5 requisições de diferentes clientes
            for (int i = 1; i <= 5; i++) {
                AIArticleRequest request = new AIArticleRequest(
                    (long) i, USER_ID + i, CATEGORY, "Article " + i, null
                );

                HttpEntity<AIArticleRequest> httpRequest = new HttpEntity<>(request, headers);

                ResponseEntity<ArticleResponse> response = restTemplate.exchange(
                    baseUrl + "/search",
                    HttpMethod.POST,
                    httpRequest,
                    ArticleResponse.class
                );

                // Assert individual
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
            }

            // Assert global - Todas requisições processadas
            verify(articleRepository, atLeast(5)).findByTopic(anyString());
            verify(kafkaProducer, times(5)).sendResponse(anyString(), any());
        }

        @Test
        @Order(13)
        @DisplayName("E2E-013: Requisições sequenciais mantêm isolamento")
        void shouldMaintainIsolationBetweenSequentialRequests() {
            // Arrange
            MedicalArticle article1 = new MedicalArticle("Content 1 " + "A".repeat(100), "https://example.com/1");
            MedicalArticle article2 = new MedicalArticle("Content 2 " + "B".repeat(100), "https://example.com/2");

            AIProcessingPort.AIProcessingResult aiResult1 = new AIProcessingPort.AIProcessingResult(
                "Title 1", "Intro 1", List.of(), "Conclusion 1", List.of(), LocalDateTime.now()
            );
            AIProcessingPort.AIProcessingResult aiResult2 = new AIProcessingPort.AIProcessingResult(
                "Title 2", "Intro 2", List.of(), "Conclusion 2", List.of(), LocalDateTime.now()
            );

            when(articleRepository.findByTopic("Topic 1")).thenReturn(Optional.of(article1));
            when(articleRepository.findByTopic("Topic 2")).thenReturn(Optional.of(article2));
            when(aiProcessing.processArticle(eq("Topic 1"), any())).thenReturn(aiResult1);
            when(aiProcessing.processArticle(eq("Topic 2"), any())).thenReturn(aiResult2);
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act - Request 1
            AIArticleRequest request1 = new AIArticleRequest(1L, 111L, CATEGORY, "Topic 1", null);
            HttpEntity<AIArticleRequest> httpRequest1 = new HttpEntity<>(request1, headers);

            ResponseEntity<ArticleResponse> response1 = restTemplate.exchange(
                baseUrl + "/search", HttpMethod.POST, httpRequest1, ArticleResponse.class
            );

            // Act - Request 2
            AIArticleRequest request2 = new AIArticleRequest(2L, 222L, CATEGORY, "Topic 2", null);
            HttpEntity<AIArticleRequest> httpRequest2 = new HttpEntity<>(request2, headers);

            ResponseEntity<ArticleResponse> response2 = restTemplate.exchange(
                baseUrl + "/search", HttpMethod.POST, httpRequest2, ArticleResponse.class
            );

            // Assert - Respostas isoladas
            assertThat(Objects.requireNonNull(response1.getBody()).title()).isEqualTo("Title 1");
            assertThat(response1.getBody().introduction()).isEqualTo("Intro 1");

            assertThat(Objects.requireNonNull(response2.getBody()).title()).isEqualTo("Title 2");
            assertThat(response2.getBody().introduction()).isEqualTo("Intro 2");

            // Verificar isolamento no Kafka
            verify(kafkaProducer).sendResponse(eq("111"), any());
            verify(kafkaProducer).sendResponse(eq("222"), any());
        }
    }

    @Nested
    @DisplayName("7. Testes de Diferentes Fontes de Artigos")
    class DifferentArticleSources {

        @Test
        @Order(14)
        @DisplayName("E2E-014: Processar artigo de fonte confiável - CREMESP")
        void shouldProcessArticleFromCremespSource() {
            testArticleFromSource("https://cremesp.org.br/artigo/xyz", "CREMESP Article");
        }

        @Test
        @Order(15)
        @DisplayName("E2E-015: Processar artigo de fonte confiável - PubMed")
        void shouldProcessArticleFromPubMedSource() {
            testArticleFromSource("https://pubmed.gov/article/12345", "PubMed Article");
        }

        @Test
        @Order(16)
        @DisplayName("E2E-016: Processar artigo de fonte confiável - SciELO")
        void shouldProcessArticleFromScieloSource() {
            testArticleFromSource("https://scielo.br/article/abc", "SciELO Article");
        }

        @Test
        @Order(17)
        @DisplayName("E2E-017: Processar artigo de fonte não confiável com warning")
        void shouldProcessArticleFromUntrustedSource() {
            testArticleFromSource("https://blog.example.com/health", "Blog Article");
        }

        private void testArticleFromSource(String sourceUrl, String title) {
            // Arrange
            MedicalArticle article = new MedicalArticle("A".repeat(200), sourceUrl);
            AIProcessingPort.AIProcessingResult aiResult = new AIProcessingPort.AIProcessingResult(
                title, "Introduction", List.of(), "Conclusion", List.of(), LocalDateTime.now()
            );

            AIArticleRequest request = new AIArticleRequest(1L, USER_ID, CATEGORY, title, null);

            when(articleRepository.findByTopic(title)).thenReturn(Optional.of(article));
            when(aiProcessing.processArticle(eq(title), any())).thenReturn(aiResult);
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            HttpEntity<AIArticleRequest> httpRequest = new HttpEntity<>(request, headers);

            // Act
            ResponseEntity<ArticleResponse> response = restTemplate.exchange(
                baseUrl + "/search", HttpMethod.POST, httpRequest, ArticleResponse.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(Objects.requireNonNull(response.getBody()).sourceLink()).isEqualTo(sourceUrl);
            assertThat(response.getBody().title()).isEqualTo(title);

            verify(kafkaProducer).sendResponse(anyString(), any());
        }
    }

    @AfterEach
    void cleanup() {
        reset(articleRepository, aiProcessing, kafkaProducer);
    }
}
