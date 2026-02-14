package com.fiap.brain.health.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.brain.health.api.dto.request.AIArticleRequest;
import com.fiap.brain.health.application.mapper.ArticleResponseMapper;
import com.fiap.brain.health.application.usecase.SearchAndGenerateArticleUseCase;
import com.fiap.brain.health.domain.exception.ArticleNotFoundException;
import com.fiap.brain.health.domain.exception.InsufficientContentException;
import com.fiap.brain.health.domain.model.MedicalArticle;
import com.fiap.brain.health.domain.port.AIProcessingPort;
import com.fiap.brain.health.domain.port.MedicalArticleRepositoryPort;
import com.fiap.brain.health.infrastructure.adapter.kafka.BrainHealthKafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AIArticleController.class)
@Import(ArticleResponseMapper.class)
@DisplayName("AIArticleController - Testes de Integração")
class AIArticleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SearchAndGenerateArticleUseCase searchAndGenerateUseCase;

    @MockBean
    private MedicalArticleRepositoryPort articleRepository;

    @MockBean
    private BrainHealthKafkaProducer kafkaProducer;

    private AIArticleRequest validRequest;
    private MedicalArticle article;
    private AIProcessingPort.AIProcessingResult aiResult;

    private static final String API_PATH = "/api/v1/ai/articles/search";
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
            "A".repeat(200),
            "https://cremesp.org.br/article/123"
        );

        aiResult = new AIProcessingPort.AIProcessingResult(
            "Benefícios da Caminhada",
            "A caminhada é uma atividade física benéfica.",
            List.of(
                new AIProcessingPort.RecommendationItem(
                    "Frequência",
                    "Caminhe 30 minutos/dia",
                    List.of("Comece devagar", "Aumente gradualmente")
                )
            ),
            "Pratique regularmente.",
            List.of(
                new AIProcessingPort.QuizItem(
                    "Duração recomendada?",
                    List.of("15 min", "30 min", "60 min"),
                    "30 min"
                )
            ),
            LocalDateTime.of(2026, 2, 11, 10, 30)
        );
    }

    @Nested
    @DisplayName("1. POST /api/v1/ai/articles/search - Cenários de Sucesso")
    class SuccessScenarios {

        @Test
        @DisplayName("Deve retornar 200 OK com artigo processado com sucesso")
        void shouldReturn200WithProcessedArticle() throws Exception {
            // Arrange
            when(searchAndGenerateUseCase.execute(TITLE)).thenReturn(aiResult);
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(article));
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act & Assert
            mockMvc.perform(post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Benefícios da Caminhada"))
                    .andExpect(jsonPath("$.introduction").value("A caminhada é uma atividade física benéfica."))
                    .andExpect(jsonPath("$.conclusion").value("Pratique regularmente."))
                    .andExpect(jsonPath("$.sourceLink").value("https://cremesp.org.br/article/123"))
                    .andExpect(jsonPath("$.recommendations").isArray())
                    .andExpect(jsonPath("$.recommendations", hasSize(1)))
                    .andExpect(jsonPath("$.quizzes").isArray())
                    .andExpect(jsonPath("$.quizzes", hasSize(1)))
                    .andExpect(jsonPath("$.timestamp").exists());

            // Verify interactions
            verify(searchAndGenerateUseCase).execute(TITLE);
            verify(articleRepository).findByTopic(TITLE);
            verify(kafkaProducer).sendResponse(anyString(), any());
        }

        @Test
        @DisplayName("Deve retornar ArticleResponse com recommendations completas")
        void shouldReturnCompleteRecommendations() throws Exception {
            // Arrange
            when(searchAndGenerateUseCase.execute(TITLE)).thenReturn(aiResult);
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(article));
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act & Assert
            mockMvc.perform(post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.recommendations[0].category").value("Frequência"))
                    .andExpect(jsonPath("$.recommendations[0].description").value("Caminhe 30 minutos/dia"))
                    .andExpect(jsonPath("$.recommendations[0].tips").isArray())
                    .andExpect(jsonPath("$.recommendations[0].tips", hasSize(2)))
                    .andExpect(jsonPath("$.recommendations[0].tips[0]").value("Comece devagar"));
        }

        @Test
        @DisplayName("Deve retornar ArticleResponse com quizzes completos")
        void shouldReturnCompleteQuizzes() throws Exception {
            // Arrange
            when(searchAndGenerateUseCase.execute(TITLE)).thenReturn(aiResult);
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(article));
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act & Assert
            mockMvc.perform(post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quizzes[0].question").value("Duração recomendada?"))
                    .andExpect(jsonPath("$.quizzes[0].options").isArray())
                    .andExpect(jsonPath("$.quizzes[0].options", hasSize(3)))
                    .andExpect(jsonPath("$.quizzes[0].correctAnswer").value("30 min"));
        }

        @Test
        @DisplayName("Deve enviar mensagem ao Kafka após sucesso")
        void shouldSendKafkaMessageAfterSuccess() throws Exception {
            // Arrange
            when(searchAndGenerateUseCase.execute(TITLE)).thenReturn(aiResult);
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(article));
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act
            mockMvc.perform(post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());

            // Assert
            verify(kafkaProducer, times(1)).sendResponse(eq(String.valueOf(USER_ID)), any());
        }
    }

    @Nested
    @DisplayName("2. POST /api/v1/ai/articles/search - Validações de Request")
    class RequestValidationScenarios {

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
            mockMvc.perform(post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(searchAndGenerateUseCase, never()).execute(any());
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
            mockMvc.perform(post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());

            verify(searchAndGenerateUseCase, never()).execute(any());
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
            mockMvc.perform(post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());

            verify(searchAndGenerateUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando body for inválido")
        void shouldReturn400WhenBodyIsInvalid() throws Exception {
            // Arrange
            String malformedJson = "{ invalid json }";

            // Act & Assert
            mockMvc.perform(post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson))
                    .andExpect(status().isBadRequest());
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

            when(searchAndGenerateUseCase.execute(TITLE)).thenReturn(aiResult);
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(article));
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act & Assert
            mockMvc.perform(post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validJson))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("3. POST /api/v1/ai/articles/search - Cenários de Erro")
    class ErrorScenarios {

        @Test
        @DisplayName("Deve retornar 404 Not Found quando artigo não existe")
        void shouldReturn404WhenArticleNotFound() throws Exception {
            // Arrange
            when(searchAndGenerateUseCase.execute(TITLE))
                .thenThrow(ArticleNotFoundException.forTopic(TITLE));

            // Act & Assert
            mockMvc.perform(post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(kafkaProducer, never()).sendResponse(anyString(), any());
        }

        @Test
        @DisplayName("Deve retornar 422 Unprocessable Entity quando conteúdo é insuficiente")
        void shouldReturn422WhenContentInsufficient() throws Exception {
            // Arrange
            when(searchAndGenerateUseCase.execute(TITLE))
                .thenThrow(new InsufficientContentException(50, 100));

            // Act & Assert
            mockMvc.perform(post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isUnprocessableEntity());

            verify(kafkaProducer, never()).sendResponse(anyString(), any());
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error quando AI processing falha")
        void shouldReturn500WhenAIProcessingFails() throws Exception {
            // Arrange
            when(searchAndGenerateUseCase.execute(TITLE))
                .thenThrow(new AIProcessingPort.AIProcessingException("OpenAI API error"));

            // Act & Assert
            mockMvc.perform(post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());

            verify(kafkaProducer, never()).sendResponse(anyString(), any());
        }
    }

    @Nested
    @DisplayName("4. Validação de Headers e Content-Type")
    class HeaderValidationScenarios {

        @Test
        @DisplayName("Deve aceitar Content-Type application/json")
        void shouldAcceptApplicationJson() throws Exception {
            // Arrange
            when(searchAndGenerateUseCase.execute(TITLE)).thenReturn(aiResult);
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(article));
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act & Assert
            mockMvc.perform(post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Deve retornar 415 Unsupported Media Type quando Content-Type não é JSON")
        void shouldReturn415WhenContentTypeIsNotJson() throws Exception {
            // Act & Assert
            mockMvc.perform(post(API_PATH)
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Nested
    @DisplayName("5. Integração Completa - Fluxo End-to-End")
    class EndToEndIntegrationScenarios {

        @Test
        @DisplayName("Deve executar fluxo completo: Request → UseCase → Repository → Mapper → Kafka → Response")
        void shouldExecuteCompleteFlow() throws Exception {
            // Arrange
            when(searchAndGenerateUseCase.execute(TITLE)).thenReturn(aiResult);
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(article));
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act
            mockMvc.perform(post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());

            // Assert - Verify complete flow
            var inOrder = inOrder(searchAndGenerateUseCase, articleRepository, kafkaProducer);
            inOrder.verify(searchAndGenerateUseCase).execute(TITLE);
            inOrder.verify(articleRepository).findByTopic(TITLE);
            inOrder.verify(kafkaProducer).sendResponse(anyString(), any());
        }

        @Test
        @DisplayName("Deve processar diferentes títulos corretamente")
        void shouldProcessDifferentTitlesCorrectly() throws Exception {
            // Arrange
            String customTitle = "Diabetes e Alimentação";
            AIArticleRequest customRequest = new AIArticleRequest(
                1L, USER_ID, "SAUDE_FISICA", customTitle, "Artigo sobre diabetes"
            );

            AIProcessingPort.AIProcessingResult customResult = new AIProcessingPort.AIProcessingResult(
                customTitle,
                "Introdução sobre diabetes",
                List.of(),
                "Conclusão",
                List.of(),
                LocalDateTime.now()
            );

            when(searchAndGenerateUseCase.execute(customTitle)).thenReturn(customResult);
            when(articleRepository.findByTopic(customTitle)).thenReturn(Optional.of(article));
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act & Assert
            mockMvc.perform(post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(customRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value(customTitle));

            verify(searchAndGenerateUseCase).execute(customTitle);
            verify(articleRepository).findByTopic(customTitle);
        }
    }

    @Nested
    @DisplayName("6. Testes de Performance e Carga")
    class PerformanceScenarios {

        @Test
        @DisplayName("Deve processar múltiplas requisições independentemente")
        void shouldProcessMultipleRequestsIndependently() throws Exception {
            // Arrange
            when(searchAndGenerateUseCase.execute(anyString())).thenReturn(aiResult);
            when(articleRepository.findByTopic(anyString())).thenReturn(Optional.of(article));
            doNothing().when(kafkaProducer).sendResponse(anyString(), any());

            // Act & Assert - Multiple requests
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                        .andExpect(status().isOk());
            }

            verify(searchAndGenerateUseCase, times(5)).execute(TITLE);
            verify(kafkaProducer, times(5)).sendResponse(anyString(), any());
        }
    }
}
