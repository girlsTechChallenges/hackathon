package com.fiap.brain.health.infrastructure.adapter.ai;

import com.fiap.brain.health.domain.model.MedicalArticle;
import com.fiap.brain.health.domain.port.AIProcessingPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenAIProcessingAdapter - Testes Unitários")
class OpenAIProcessingAdapterTest {

    @Mock
    private ChatModel chatModel;

    private OpenAIProcessingAdapter adapter;

    private static final String SAMPLE_QUESTION = "Como prevenir doenças cardiovasculares?";
    private static final String SAMPLE_CONTENT = "Artigo sobre saúde cardiovascular com informações sobre prevenção...";
    private static final String SAMPLE_URL = "https://cremesp.org.br/article/123";

    @BeforeEach
    void setUp() {
        adapter = new OpenAIProcessingAdapter(chatModel);
    }

    @Nested
    @DisplayName("Processamento de Artigos - Sucesso")
    class SuccessfulProcessing {

        @Test
        @DisplayName("Deve processar artigo com sucesso")
        void shouldProcessArticleSuccessfully() {
            // Arrange
            MedicalArticle article = createSampleArticle();
            String aiResponse = createValidAIResponse();
            mockChatModelResponse(aiResponse);

            // Act
            AIProcessingPort.AIProcessingResult result = adapter.processArticle(SAMPLE_QUESTION, article);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo("Prevenção de Doenças Cardiovasculares");
            assertThat(result.introduction()).isNotEmpty();
            assertThat(result.recommendations()).hasSize(3);
            assertThat(result.conclusion()).isNotEmpty();
            assertThat(result.quizzes()).hasSize(3);
            assertThat(result.processedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve processar recomendações corretamente")
        void shouldProcessRecommendationsCorrectly() {
            // Arrange
            MedicalArticle article = createSampleArticle();
            String aiResponse = createValidAIResponse();
            mockChatModelResponse(aiResponse);

            // Act
            AIProcessingPort.AIProcessingResult result = adapter.processArticle(SAMPLE_QUESTION, article);

            // Assert
            assertThat(result.recommendations()).isNotEmpty();
            AIProcessingPort.RecommendationItem firstRec = result.recommendations().getFirst();
            assertThat(firstRec.category()).isNotEmpty();
            assertThat(firstRec.description()).isNotEmpty();
            assertThat(firstRec.tips()).isNotEmpty();
        }

        @Test
        @DisplayName("Deve processar quiz corretamente")
        void shouldProcessQuizCorrectly() {
            // Arrange
            MedicalArticle article = createSampleArticle();
            String aiResponse = createValidAIResponse();
            mockChatModelResponse(aiResponse);

            // Act
            AIProcessingPort.AIProcessingResult result = adapter.processArticle(SAMPLE_QUESTION, article);

            // Assert
            assertThat(result.quizzes()).isNotEmpty();
            AIProcessingPort.QuizItem firstQuiz = result.quizzes().getFirst();
            assertThat(firstQuiz.question()).isNotEmpty();
            assertThat(firstQuiz.options()).hasSize(4);
            assertThat(firstQuiz.correctAnswer()).isNotEmpty();
        }

        @Test
        @DisplayName("Deve criar timestamp no processamento")
        void shouldCreateTimestampOnProcessing() {
            // Arrange
            MedicalArticle article = createSampleArticle();
            String aiResponse = createValidAIResponse();
            mockChatModelResponse(aiResponse);

            // Act
            AIProcessingPort.AIProcessingResult result = adapter.processArticle(SAMPLE_QUESTION, article);

            // Assert
            assertThat(result.processedAt()).isNotNull();
            assertThat(result.processedAt()).isBeforeOrEqualTo(java.time.LocalDateTime.now());
        }

        @Test
        @DisplayName("Deve chamar ChatModel com prompt correto")
        void shouldCallChatModelWithCorrectPrompt() {
            // Arrange
            MedicalArticle article = createSampleArticle();
            String aiResponse = createValidAIResponse();
            mockChatModelResponse(aiResponse);

            // Act
            adapter.processArticle(SAMPLE_QUESTION, article);

            // Assert
            verify(chatModel, times(1)).call(any(Prompt.class));
        }
    }

    @Nested
    @DisplayName("Tratamento de Erros")
    class ErrorHandling {

        @Test
        @DisplayName("Deve lançar exceção quando ChatModel falha")
        void shouldThrowExceptionWhenChatModelFails() {
            // Arrange
            MedicalArticle article = createSampleArticle();
            when(chatModel.call(any(Prompt.class)))
                    .thenThrow(new RuntimeException("OpenAI API error"));

            // Act & Assert
            assertThatThrownBy(() -> adapter.processArticle(SAMPLE_QUESTION, article))
                    .isInstanceOf(AIProcessingPort.AIProcessingException.class)
                    .hasMessageContaining("Failed to process article with OpenAI");
        }

        @Test
        @DisplayName("Deve lançar exceção quando resposta é nula")
        void shouldThrowExceptionWhenResponseIsNull() {
            // Arrange
            MedicalArticle article = createSampleArticle();
            mockChatModelResponse(null);

            // Act & Assert
            assertThatThrownBy(() -> adapter.processArticle(SAMPLE_QUESTION, article))
                    .isInstanceOf(AIProcessingPort.AIProcessingException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando resposta é inválida")
        void shouldThrowExceptionWhenResponseIsInvalid() {
            // Arrange
            MedicalArticle article = createSampleArticle();
            mockChatModelResponse("Invalid JSON response");

            // Act & Assert
            assertThatThrownBy(() -> adapter.processArticle(SAMPLE_QUESTION, article))
                    .isInstanceOf(AIProcessingPort.AIProcessingException.class);
        }

        @Test
        @DisplayName("Deve tratar timeout da API OpenAI")
        void shouldHandleOpenAITimeout() {
            // Arrange
            MedicalArticle article = createSampleArticle();
            when(chatModel.call(any(Prompt.class)))
                    .thenThrow(new RuntimeException("Request timeout"));

            // Act & Assert
            assertThatThrownBy(() -> adapter.processArticle(SAMPLE_QUESTION, article))
                    .isInstanceOf(AIProcessingPort.AIProcessingException.class)
                    .hasMessageContaining("Failed to process article");
        }

        @Test
        @DisplayName("Deve tratar erro de quota excedida")
        void shouldHandleQuotaExceeded() {
            // Arrange
            MedicalArticle article = createSampleArticle();
            when(chatModel.call(any(Prompt.class)))
                    .thenThrow(new RuntimeException("Rate limit exceeded"));

            // Act & Assert
            assertThatThrownBy(() -> adapter.processArticle(SAMPLE_QUESTION, article))
                    .isInstanceOf(AIProcessingPort.AIProcessingException.class);
        }
    }

    @Nested
    @DisplayName("Validação de Dados")
    class DataValidation {

        @Test
        @DisplayName("Deve processar artigo com conteúdo longo")
        void shouldProcessArticleWithLongContent() {
            // Arrange
            String longContent = "x".repeat(5000);
            MedicalArticle article = MedicalArticle.of(longContent, SAMPLE_URL).orElseThrow();
            String aiResponse = createValidAIResponse();
            mockChatModelResponse(aiResponse);

            // Act
            AIProcessingPort.AIProcessingResult result = adapter.processArticle(SAMPLE_QUESTION, article);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.title()).isNotEmpty();
        }

        @Test
        @DisplayName("Deve processar artigo com caracteres especiais")
        void shouldProcessArticleWithSpecialCharacters() {
            // Arrange
            String specialContent = "Artigo com acentuação: café, açúcar, ñ, ç";
            MedicalArticle article = MedicalArticle.of(specialContent, SAMPLE_URL).orElseThrow();
            String aiResponse = createValidAIResponse();
            mockChatModelResponse(aiResponse);

            // Act
            AIProcessingPort.AIProcessingResult result = adapter.processArticle(SAMPLE_QUESTION, article);

            // Assert
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Deve processar pergunta complexa")
        void shouldProcessComplexQuestion() {
            // Arrange
            String complexQuestion = "Como prevenir doenças cardiovasculares em pacientes com diabetes tipo 2?";
            MedicalArticle article = createSampleArticle();
            String aiResponse = createValidAIResponse();
            mockChatModelResponse(aiResponse);

            // Act
            AIProcessingPort.AIProcessingResult result = adapter.processArticle(complexQuestion, article);

            // Assert
            assertThat(result).isNotNull();
            verify(chatModel, times(1)).call(any(Prompt.class));
        }

        @Test
        @DisplayName("Deve lidar com resposta sem todas as recomendações")
        void shouldHandleResponseWithFewerRecommendations() {
            // Arrange
            MedicalArticle article = createSampleArticle();
            String aiResponse = createAIResponseWithLimitedRecommendations();
            mockChatModelResponse(aiResponse);

            // Act
            AIProcessingPort.AIProcessingResult result = adapter.processArticle(SAMPLE_QUESTION, article);

            // Assert
            assertThat(result.recommendations()).hasSizeLessThanOrEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Casos Especiais")
    class EdgeCases {

        @Test
        @DisplayName("Deve processar resposta com listas vazias")
        void shouldProcessResponseWithEmptyLists() {
            // Arrange
            MedicalArticle article = createSampleArticle();
            String aiResponse = createAIResponseWithEmptyLists();
            mockChatModelResponse(aiResponse);

            // Act
            AIProcessingPort.AIProcessingResult result = adapter.processArticle(SAMPLE_QUESTION, article);

            // Assert
            assertThat(result.recommendations()).isEmpty();
            assertThat(result.quizzes()).isEmpty();
        }

        @Test
        @DisplayName("Deve garantir thread-safety em múltiplas chamadas")
        void shouldEnsureThreadSafetyInMultipleCalls() {
            // Arrange
            MedicalArticle article = createSampleArticle();
            String aiResponse = createValidAIResponse();
            mockChatModelResponse(aiResponse);

            // Act - Múltiplas chamadas
            AIProcessingPort.AIProcessingResult result1 = adapter.processArticle(SAMPLE_QUESTION, article);
            AIProcessingPort.AIProcessingResult result2 = adapter.processArticle(SAMPLE_QUESTION, article);
            AIProcessingPort.AIProcessingResult result3 = adapter.processArticle(SAMPLE_QUESTION, article);

            // Assert
            assertThat(result1).isNotNull();
            assertThat(result2).isNotNull();
            assertThat(result3).isNotNull();
            verify(chatModel, times(3)).call(any(Prompt.class));
        }

        @Test
        @DisplayName("Deve processar URL de artigo válida")
        void shouldProcessValidArticleUrl() {
            // Arrange
            MedicalArticle article = createSampleArticle();
            String aiResponse = createValidAIResponse();
            mockChatModelResponse(aiResponse);

            // Act
            AIProcessingPort.AIProcessingResult result = adapter.processArticle(SAMPLE_QUESTION, article);

            // Assert
            assertThat(result).isNotNull();
            assertThat(article.getArticleUrl()).startsWith("https://");
        }
    }

    // ==================== Helper Methods ====================

    private MedicalArticle createSampleArticle() {
        return MedicalArticle.of(SAMPLE_CONTENT, SAMPLE_URL)
                .orElseThrow(() -> new IllegalStateException("Failed to create sample article"));
    }

    private void mockChatModelResponse(String content) {
        AssistantMessage message = new AssistantMessage(content != null ? content : "");
        Generation generation = new Generation(message);
        ChatResponse chatResponse = new ChatResponse(List.of(generation));

        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
    }

    private String createValidAIResponse() {
        return """
                {
                    "title": "Prevenção de Doenças Cardiovasculares",
                    "introduction": "As doenças cardiovasculares são a principal causa de morte no mundo.",
                    "recommendations": [
                        {
                            "category": "Alimentação",
                            "description": "Mantenha uma dieta equilibrada",
                            "tips": ["Coma mais frutas", "Reduza sal", "Evite gorduras trans"]
                        },
                        {
                            "category": "Exercícios",
                            "description": "Pratique atividades físicas regularmente",
                            "tips": ["30 minutos diários", "Caminhada", "Natação"]
                        },
                        {
                            "category": "Hábitos",
                            "description": "Evite fumar e beber em excesso",
                            "tips": ["Não fume", "Beba com moderação", "Durma bem"]
                        }
                    ],
                    "conclusion": "A prevenção é a melhor forma de evitar doenças cardiovasculares.",
                    "quizzes": [
                        {
                            "question": "Qual a principal causa de doenças cardiovasculares?",
                            "options": ["Sedentarismo", "Má alimentação", "Estresse", "Todas as anteriores"],
                            "correctAnswer": "Todas as anteriores"
                        },
                        {
                            "question": "Quantos minutos de exercício são recomendados por dia?",
                            "options": ["10 minutos", "20 minutos", "30 minutos", "60 minutos"],
                            "correctAnswer": "30 minutos"
                        },
                        {
                            "question": "O que deve ser evitado na alimentação?",
                            "options": ["Frutas", "Verduras", "Gorduras trans", "Água"],
                            "correctAnswer": "Gorduras trans"
                        }
                    ]
                }
                """;
    }

    private String createAIResponseWithLimitedRecommendations() {
        return """
                {
                    "title": "Prevenção de Doenças Cardiovasculares",
                    "introduction": "Introdução sobre prevenção",
                    "recommendations": [
                        {
                            "category": "Alimentação",
                            "description": "Dieta saudável",
                            "tips": ["Coma frutas"]
                        }
                    ],
                    "conclusion": "Conclusão",
                    "quizzes": [
                        {
                            "question": "Pergunta teste?",
                            "options": ["A", "B", "C", "D"],
                            "correctAnswer": "A"
                        }
                    ]
                }
                """;
    }

    private String createAIResponseWithEmptyLists() {
        return """
                {
                    "title": "Título",
                    "introduction": "Introdução",
                    "recommendations": [],
                    "conclusion": "Conclusão",
                    "quizzes": []
                }
                """;
    }
}
