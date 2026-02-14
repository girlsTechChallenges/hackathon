package com.fiap.brain.health.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.brain.health.api.dto.request.AIArticleRequest;
import com.fiap.brain.health.api.dto.response.ArticleResponse;
import com.fiap.brain.health.domain.model.MedicalArticle;
import com.fiap.brain.health.domain.port.AIProcessingPort;
import com.fiap.brain.health.domain.port.MedicalArticleRepositoryPort;
import com.fiap.brain.health.infrastructure.adapter.kafka.BrainHealthKafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de Integração Completos - Spring Boot")
class FullStackIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MedicalArticleRepositoryPort articleRepository;

    @MockBean
    private AIProcessingPort aiProcessing;

    @MockBean
    private BrainHealthKafkaProducer kafkaProducer;

    private AIArticleRequest validRequest;
    private MedicalArticle article;
    private AIProcessingPort.AIProcessingResult aiResult;

    private static final String ARTICLE_SEARCH_PATH = "/api/v1/ai/articles/search";
    private static final Long USER_ID = 12345L;
    private static final String TITLE = "Benefícios da Caminhada";

    @BeforeEach
    void setUp() {
        validRequest = new AIArticleRequest(
            1L,
            USER_ID,
            "SAUDE_FISICA",
            TITLE,
            "Artigo sobre caminhada"
        );

        article = new MedicalArticle(
            "A caminhada regular é uma das formas mais acessíveis e eficazes de exercício físico. " +
            "Estudos científicos demonstram que caminhar 30 minutos por dia pode reduzir significativamente " +
            "o risco de doenças cardiovasculares, melhorar a saúde mental e fortalecer o sistema imunológico.",
            "https://cremesp.org.br/artigos/beneficios-caminhada"
        );

        aiResult = new AIProcessingPort.AIProcessingResult(
            "Benefícios da Caminhada para a Saúde",
            "A caminhada é uma atividade física simples, acessível e extremamente benéfica para a saúde.",
            List.of(
                new AIProcessingPort.RecommendationItem(
                    "Frequência e Duração",
                    "Caminhe pelo menos 30 minutos por dia, 5 vezes por semana",
                    List.of(
                        "Comece com 10-15 minutos e aumente gradualmente",
                        "Escolha horários com temperatura agradável",
                        "Use calçados confortáveis e adequados"
                    )
                ),
                new AIProcessingPort.RecommendationItem(
                    "Intensidade",
                    "Mantenha um ritmo moderado onde você consegue conversar",
                    List.of(
                        "Monitore sua frequência cardíaca",
                        "Mantenha postura ereta durante a caminhada"
                    )
                ),
                new AIProcessingPort.RecommendationItem(
                    "Segurança",
                    "Caminhe em locais seguros e bem iluminados",
                    List.of(
                        "Prefira parques e calçadas regulares",
                        "Evite horários de muito calor",
                        "Mantenha-se hidratado"
                    )
                )
            ),
            "A caminhada regular traz benefícios significativos para a saúde física e mental, " +
            "sendo uma excelente forma de iniciar ou manter um estilo de vida ativo.",
            List.of(
                new AIProcessingPort.QuizItem(
                    "Qual a duração mínima recomendada de caminhada por dia?",
                    List.of("10 minutos", "20 minutos", "30 minutos", "60 minutos"),
                    "30 minutos"
                ),
                new AIProcessingPort.QuizItem(
                    "Quantas vezes por semana é recomendado caminhar?",
                    List.of("2 vezes", "3 vezes", "5 vezes", "Todos os dias"),
                    "5 vezes"
                ),
                new AIProcessingPort.QuizItem(
                    "Qual a intensidade ideal da caminhada?",
                    List.of("Muito leve", "Moderada", "Intensa", "Máxima"),
                    "Moderada"
                )
            ),
            LocalDateTime.of(2026, 2, 11, 14, 30)
        );
    }

    @Nested
    @DisplayName("1. Integração Completa - Controller → UseCase → Mapper → Repository")
    class CompleteFlowIntegrationTests {

        @Test
        @DisplayName("Deve processar request completo através de toda stack")
        void shouldProcessCompleteRequestThroughEntireStack() throws Exception {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(article));
            when(aiProcessing.processArticle(eq(TITLE), any(MedicalArticle.class))).thenReturn(aiResult);
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act
            MvcResult result = mockMvc.perform(post(ARTICLE_SEARCH_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert Response Structure
            String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
            ArticleResponse response = objectMapper.readValue(responseBody, ArticleResponse.class);

            assertThat(response).isNotNull();
            assertThat(response.title()).isEqualTo("Benefícios da Caminhada para a Saúde");
            assertThat(response.introduction()).contains("simples, acessível");
            assertThat(response.recommendations()).hasSize(3);
            assertThat(response.quizzes()).hasSize(3);
            assertThat(response.sourceLink()).isEqualTo("https://cremesp.org.br/artigos/beneficios-caminhada");

            // Verify all interactions
            verify(articleRepository, times(2)).findByTopic(TITLE); // Called twice: UseCase + Controller
            verify(aiProcessing).processArticle(eq(TITLE), any(MedicalArticle.class));
            verify(kafkaProducer).sendResponse(eq(String.valueOf(USER_ID)), any());
        }

        @Test
        @DisplayName("Deve mapear corretamente todas as recommendations do AI para DTO")
        void shouldMapAllRecommendationsCorrectly() throws Exception {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(article));
            when(aiProcessing.processArticle(eq(TITLE), any(MedicalArticle.class))).thenReturn(aiResult);
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act & Assert
            mockMvc.perform(post(ARTICLE_SEARCH_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    // First recommendation
                    .andExpect(jsonPath("$.recommendations[0].category").value("Frequência e Duração"))
                    .andExpect(jsonPath("$.recommendations[0].description").value(containsString("30 minutos")))
                    .andExpect(jsonPath("$.recommendations[0].tips").isArray())
                    .andExpect(jsonPath("$.recommendations[0].tips", hasSize(3)))
                    // Second recommendation
                    .andExpect(jsonPath("$.recommendations[1].category").value("Intensidade"))
                    .andExpect(jsonPath("$.recommendations[1].tips", hasSize(2)))
                    // Third recommendation
                    .andExpect(jsonPath("$.recommendations[2].category").value("Segurança"))
                    .andExpect(jsonPath("$.recommendations[2].tips", hasSize(3)));
        }

        @Test
        @DisplayName("Deve mapear corretamente todos os quizzes do AI para DTO")
        void shouldMapAllQuizzesCorrectly() throws Exception {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(article));
            when(aiProcessing.processArticle(eq(TITLE), any(MedicalArticle.class))).thenReturn(aiResult);
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act & Assert
            mockMvc.perform(post(ARTICLE_SEARCH_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    // First quiz
                    .andExpect(jsonPath("$.quizzes[0].question").value(containsString("duração mínima")))
                    .andExpect(jsonPath("$.quizzes[0].options", hasSize(4)))
                    .andExpect(jsonPath("$.quizzes[0].correctAnswer").value("30 minutos"))
                    // Second quiz
                    .andExpect(jsonPath("$.quizzes[1].question").value(containsString("vezes por semana")))
                    .andExpect(jsonPath("$.quizzes[1].correctAnswer").value("5 vezes"))
                    // Third quiz
                    .andExpect(jsonPath("$.quizzes[2].question").value(containsString("intensidade")))
                    .andExpect(jsonPath("$.quizzes[2].correctAnswer").value("Moderada"));
        }
    }

    @Nested
    @DisplayName("2. Integração com UseCase - Regras de Negócio")
    class UseCaseBusinessRulesIntegrationTests {

        @Test
        @DisplayName("Deve validar conteúdo mínimo através do UseCase")
        void shouldValidateMinimumContentThroughUseCase() throws Exception {
            // Arrange - Article with insufficient content
            MedicalArticle shortArticle = new MedicalArticle(
                "Conteúdo muito curto", // Less than 100 characters
                "https://cremesp.org.br/short"
            );

            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(shortArticle));

            // Act & Assert
            mockMvc.perform(post(ARTICLE_SEARCH_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isUnprocessableEntity());

            verify(aiProcessing, never()).processArticle(any(), any());
            verify(kafkaProducer, never()).sendResponse(anyString(), any());
        }

        @Test
        @DisplayName("Deve processar artigo de fonte confiável sem erros")
        void shouldProcessTrustedSourceArticleWithoutErrors() throws Exception {
            // Arrange - Trusted sources
            String[] trustedUrls = {
                "https://cremesp.org.br/article/123",
                "https://pubmed.gov/article/456",
                "https://scielo.br/article/789"
            };

            for (String url : trustedUrls) {
                MedicalArticle trustedArticle = new MedicalArticle("A".repeat(150), url);

                when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(trustedArticle));
                when(aiProcessing.processArticle(eq(TITLE), any())).thenReturn(aiResult);
                doNothing().when(kafkaProducer).sendResponse(anyString(), any());

                // Act & Assert
                mockMvc.perform(post(ARTICLE_SEARCH_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                        .andExpect(status().isOk());

                reset(articleRepository, aiProcessing, kafkaProducer);
            }
        }
    }

    @Nested
    @DisplayName("3. Integração com Kafka Producer")
    class KafkaProducerIntegrationTests {

        @Test
        @DisplayName("Deve enviar mensagem ao Kafka com userId correto como key")
        void shouldSendKafkaMessageWithCorrectUserIdKey() throws Exception {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(article));
            when(aiProcessing.processArticle(eq(TITLE), any())).thenReturn(aiResult);
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act
            mockMvc.perform(post(ARTICLE_SEARCH_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());

            // Assert
            verify(kafkaProducer).sendResponse(
                eq(String.valueOf(USER_ID)),
                argThat(message ->
                    message.userId().equals(USER_ID) &&
                    message.status() == com.fiap.brain.health.api.dto.kafka.BrainHealthResponseMessage.ProcessingStatus.SUCCESS &&
                    message.articleResponse() != null
                )
            );
        }

        @Test
        @DisplayName("Não deve enviar mensagem ao Kafka quando ocorre erro")
        void shouldNotSendKafkaMessageWhenErrorOccurs() throws Exception {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.empty());

            // Act
            mockMvc.perform(post(ARTICLE_SEARCH_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound());

            // Assert
            verify(kafkaProducer, never()).sendResponse(anyString(), any());
        }
    }

    @Nested
    @DisplayName("4. Testes de Serialização/Deserialização JSON")
    class JsonSerializationTests {

        @Test
        @DisplayName("Deve serializar ArticleResponse corretamente para JSON")
        void shouldSerializeArticleResponseToJsonCorrectly() throws Exception {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(article));
            when(aiProcessing.processArticle(eq(TITLE), any())).thenReturn(aiResult);
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act
            MvcResult result = mockMvc.perform(post(ARTICLE_SEARCH_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert JSON Structure
            String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

            assertThat(json).contains("\"title\":");
            assertThat(json).contains("\"introduction\":");
            assertThat(json).contains("\"recommendations\":");
            assertThat(json).contains("\"quizzes\":");
            assertThat(json).contains("\"sourceLink\":");
            assertThat(json).contains("\"timestamp\":");
        }

        @Test
        @DisplayName("Deve deserializar AIArticleRequest corretamente do JSON")
        void shouldDeserializeRequestFromJsonCorrectly() throws Exception {
            // Arrange
            String complexJson = """
                {
                    "goalId": 999,
                    "userId": 54321,
                    "category": "SAUDE_MENTAL",
                    "title": "Meditação e Ansiedade",
                    "description": "Artigo científico sobre os efeitos da meditação na redução da ansiedade"
                }
                """;

            MedicalArticle customArticle = new MedicalArticle("A".repeat(150), "https://pubmed.gov/123");

            when(articleRepository.findByTopic("Meditação e Ansiedade")).thenReturn(Optional.of(customArticle));
            when(aiProcessing.processArticle(eq("Meditação e Ansiedade"), any())).thenReturn(aiResult);
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act & Assert
            mockMvc.perform(post(ARTICLE_SEARCH_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(complexJson))
                    .andExpect(status().isOk());

            verify(aiProcessing).processArticle(eq("Meditação e Ansiedade"), any());
        }
    }

    @Nested
    @DisplayName("5. Testes de Performance e Concorrência")
    class PerformanceAndConcurrencyTests {

        @Test
        @DisplayName("Deve processar múltiplas requisições sequenciais corretamente")
        void shouldProcessMultipleSequentialRequestsCorrectly() throws Exception {
            // Arrange
            when(articleRepository.findByTopic(anyString())).thenReturn(Optional.of(article));
            when(aiProcessing.processArticle(anyString(), any())).thenReturn(aiResult);
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act - Process 5 requests
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post(ARTICLE_SEARCH_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                        .andExpect(status().isOk());
            }

            // Assert
            verify(aiProcessing, times(5)).processArticle(eq(TITLE), any());
            verify(kafkaProducer, times(5)).sendResponse(anyString(), any());
        }

        @Test
        @DisplayName("Deve manter isolamento entre requisições diferentes")
        void shouldMaintainIsolationBetweenDifferentRequests() throws Exception {
            // Arrange - Different requests
            AIArticleRequest request1 = new AIArticleRequest(1L, 100L, "CAT1", "Title 1", "Desc 1");
            AIArticleRequest request2 = new AIArticleRequest(2L, 200L, "CAT2", "Title 2", "Desc 2");

            when(articleRepository.findByTopic(anyString())).thenReturn(Optional.of(article));
            when(aiProcessing.processArticle(anyString(), any())).thenReturn(aiResult);
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act - Process different requests
            mockMvc.perform(post(ARTICLE_SEARCH_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isOk());

            mockMvc.perform(post(ARTICLE_SEARCH_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isOk());

            // Assert - Verify isolation
            verify(kafkaProducer).sendResponse(eq("100"), any());
            verify(kafkaProducer).sendResponse(eq("200"), any());
        }
    }

    @Nested
    @DisplayName("6. Testes de Cenários Complexos")
    class ComplexScenariosTests {

        @Test
        @DisplayName("Deve processar artigo com recommendations e quizzes vazios")
        void shouldProcessArticleWithEmptyRecommendationsAndQuizzes() throws Exception {
            // Arrange
            AIProcessingPort.AIProcessingResult emptyResult = new AIProcessingPort.AIProcessingResult(
                "Título Simples",
                "Introdução básica",
                List.of(), // Empty recommendations
                "Conclusão básica",
                List.of(), // Empty quizzes
                LocalDateTime.now()
            );

            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(article));
            when(aiProcessing.processArticle(eq(TITLE), any())).thenReturn(emptyResult);
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act & Assert
            mockMvc.perform(post(ARTICLE_SEARCH_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.recommendations").isEmpty())
                    .andExpect(jsonPath("$.quizzes").isEmpty());
        }

        @Test
        @DisplayName("Deve processar artigo com conteúdo extenso (> 1000 caracteres)")
        void shouldProcessArticleWithLongContent() throws Exception {
            // Arrange
            String longContent = "A".repeat(5000);
            MedicalArticle longArticle = new MedicalArticle(longContent, "https://cremesp.org.br/long");

            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(longArticle));
            when(aiProcessing.processArticle(eq(TITLE), any())).thenReturn(aiResult);
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act & Assert
            mockMvc.perform(post(ARTICLE_SEARCH_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());

            verify(aiProcessing).processArticle(eq(TITLE), argThat(article ->
                article.getContentLength() == 5000
            ));
        }
    }
}
