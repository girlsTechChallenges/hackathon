package com.fiap.brain.health.application.mapper;

import com.fiap.brain.health.api.dto.response.ArticleResponse;
import com.fiap.brain.health.api.dto.response.Quiz;
import com.fiap.brain.health.api.dto.response.Recommendation;
import com.fiap.brain.health.domain.model.MedicalArticle;
import com.fiap.brain.health.domain.port.AIProcessingPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ArticleResponseMapper - Testes Unitários")
class ArticleResponseMapperTest {

    private ArticleResponseMapper mapper;

    private static final String VALID_CONTENT = "A".repeat(150);
    private static final String VALID_URL = "https://cremesp.org.br/article/123";

    private MedicalArticle article;
    private AIProcessingPort.AIProcessingResult aiResult;

    @BeforeEach
    void setUp() {
        mapper = new ArticleResponseMapper();

        article = new MedicalArticle(VALID_CONTENT, VALID_URL);

        aiResult = new AIProcessingPort.AIProcessingResult(
            "Benefícios da Caminhada",
            "A caminhada é uma atividade física acessível.",
            List.of(
                new AIProcessingPort.RecommendationItem(
                    "Frequência",
                    "Caminhe pelo menos 30 minutos por dia",
                    List.of("Comece devagar", "Aumente gradualmente", "Use tênis adequado")
                ),
                new AIProcessingPort.RecommendationItem(
                    "Intensidade",
                    "Mantenha ritmo moderado",
                    List.of("Monitore batimentos", "Respire corretamente")
                )
            ),
            "A caminhada regular traz inúmeros benefícios para a saúde.",
            List.of(
                new AIProcessingPort.QuizItem(
                    "Qual a duração mínima recomendada?",
                    List.of("15 minutos", "30 minutos", "60 minutos"),
                    "30 minutos"
                ),
                new AIProcessingPort.QuizItem(
                    "Qual o melhor horário para caminhar?",
                    List.of("Manhã", "Tarde", "Qualquer horário"),
                    "Qualquer horário"
                )
            ),
            LocalDateTime.of(2026, 2, 11, 10, 30)
        );
    }

    @Nested
    @DisplayName("1. Mapeamento Completo de ArticleResponse")
    class ArticleResponseMappingTests {

        @Test
        @DisplayName("Deve mapear AIProcessingResult e MedicalArticle para ArticleResponse completo")
        void shouldMapToCompleteArticleResponse() {
            // Act
            ArticleResponse response = mapper.toArticleResponse(aiResult, article);

            // Assert
            assertNotNull(response, "Response não deve ser nulo");
            assertEquals("Benefícios da Caminhada", response.title());
            assertEquals("A caminhada é uma atividade física acessível.", response.introduction());
            assertEquals("A caminhada regular traz inúmeros benefícios para a saúde.", response.conclusion());
            assertEquals(VALID_URL, response.sourceLink());
            assertEquals(LocalDateTime.of(2026, 2, 11, 10, 30), response.timestamp());
        }

        @Test
        @DisplayName("Deve mapear todas as recommendations corretamente")
        void shouldMapAllRecommendations() {
            // Act
            ArticleResponse response = mapper.toArticleResponse(aiResult, article);

            // Assert
            assertNotNull(response.recommendations());
            assertEquals(2, response.recommendations().size());

            Recommendation firstRec = response.recommendations().getFirst();
            assertEquals("Frequência", firstRec.category());
            assertEquals("Caminhe pelo menos 30 minutos por dia", firstRec.description());
            assertEquals(3, firstRec.tips().size());
            assertTrue(firstRec.tips().contains("Comece devagar"));

            Recommendation secondRec = response.recommendations().get(1);
            assertEquals("Intensidade", secondRec.category());
            assertEquals(2, secondRec.tips().size());
        }

        @Test
        @DisplayName("Deve mapear todos os quizzes corretamente")
        void shouldMapAllQuizzes() {
            // Act
            ArticleResponse response = mapper.toArticleResponse(aiResult, article);

            // Assert
            assertNotNull(response.quizzes());
            assertEquals(2, response.quizzes().size());

            Quiz firstQuiz = response.quizzes().getFirst();
            assertEquals("Qual a duração mínima recomendada?", firstQuiz.question());
            assertEquals(3, firstQuiz.options().size());
            assertEquals("30 minutos", firstQuiz.correctAnswer());

            Quiz secondQuiz = response.quizzes().get(1);
            assertEquals("Qual o melhor horário para caminhar?", secondQuiz.question());
            assertEquals("Qualquer horário", secondQuiz.correctAnswer());
        }

        @Test
        @DisplayName("Deve mapear corretamente quando não há recommendations")
        void shouldMapCorrectlyWhenNoRecommendations() {
            // Arrange
            AIProcessingPort.AIProcessingResult resultWithoutRecs = new AIProcessingPort.AIProcessingResult(
                "Título",
                "Introdução",
                List.of(), // Sem recommendations
                "Conclusão",
                List.of(new AIProcessingPort.QuizItem("Q?", List.of("A", "B"), "A")),
                LocalDateTime.now()
            );

            // Act
            ArticleResponse response = mapper.toArticleResponse(resultWithoutRecs, article);

            // Assert
            assertNotNull(response.recommendations());
            assertTrue(response.recommendations().isEmpty());
        }

        @Test
        @DisplayName("Deve mapear corretamente quando não há quizzes")
        void shouldMapCorrectlyWhenNoQuizzes() {
            // Arrange
            AIProcessingPort.AIProcessingResult resultWithoutQuizzes = new AIProcessingPort.AIProcessingResult(
                "Título",
                "Introdução",
                List.of(new AIProcessingPort.RecommendationItem("Cat", "Desc", List.of("Tip"))),
                "Conclusão",
                List.of(), // Sem quizzes
                LocalDateTime.now()
            );

            // Act
            ArticleResponse response = mapper.toArticleResponse(resultWithoutQuizzes, article);

            // Assert
            assertNotNull(response.quizzes());
            assertTrue(response.quizzes().isEmpty());
        }
    }

    @Nested
    @DisplayName("2. Mapeamento de Recommendation")
    class RecommendationMappingTests {

        @Test
        @DisplayName("Deve mapear RecommendationItem para Recommendation")
        void shouldMapRecommendationItem() {
            // Arrange
            AIProcessingPort.RecommendationItem item = new AIProcessingPort.RecommendationItem(
                "Alimentação",
                "Mantenha dieta balanceada",
                List.of("Coma frutas", "Beba água", "Evite frituras")
            );

            AIProcessingPort.AIProcessingResult result = new AIProcessingPort.AIProcessingResult(
                "Título",
                "Intro",
                List.of(item),
                "Conclusão",
                List.of(),
                LocalDateTime.now()
            );

            // Act
            ArticleResponse response = mapper.toArticleResponse(result, article);

            // Assert
            assertEquals(1, response.recommendations().size());
            Recommendation rec = response.recommendations().getFirst();
            assertEquals("Alimentação", rec.category());
            assertEquals("Mantenha dieta balanceada", rec.description());
            assertEquals(3, rec.tips().size());
            assertEquals("Coma frutas", rec.tips().getFirst());
        }

        @Test
        @DisplayName("Deve mapear recommendation sem tips")
        void shouldMapRecommendationWithoutTips() {
            // Arrange
            AIProcessingPort.RecommendationItem item = new AIProcessingPort.RecommendationItem(
                "Categoria",
                "Descrição",
                List.of()
            );

            AIProcessingPort.AIProcessingResult result = new AIProcessingPort.AIProcessingResult(
                "Título",
                "Intro",
                List.of(item),
                "Conclusão",
                List.of(),
                LocalDateTime.now()
            );

            // Act
            ArticleResponse response = mapper.toArticleResponse(result, article);

            // Assert
            Recommendation rec = response.recommendations().getFirst();
            assertNotNull(rec.tips());
            assertTrue(rec.tips().isEmpty());
        }
    }

    @Nested
    @DisplayName("3. Mapeamento de Quiz")
    class QuizMappingTests {

        @Test
        @DisplayName("Deve mapear QuizItem para Quiz")
        void shouldMapQuizItem() {
            // Arrange
            AIProcessingPort.QuizItem item = new AIProcessingPort.QuizItem(
                "Quantos litros de água por dia?",
                List.of("1 litro", "2 litros", "3 litros", "4 litros"),
                "2 litros"
            );

            AIProcessingPort.AIProcessingResult result = new AIProcessingPort.AIProcessingResult(
                "Título",
                "Intro",
                List.of(),
                "Conclusão",
                List.of(item),
                LocalDateTime.now()
            );

            // Act
            ArticleResponse response = mapper.toArticleResponse(result, article);

            // Assert
            assertEquals(1, response.quizzes().size());
            Quiz quiz = response.quizzes().getFirst();
            assertEquals("Quantos litros de água por dia?", quiz.question());
            assertEquals(4, quiz.options().size());
            assertEquals("2 litros", quiz.correctAnswer());
        }

        @Test
        @DisplayName("Deve mapear quiz com diferentes números de opções")
        void shouldMapQuizWithDifferentNumberOfOptions() {
            // Arrange - 2 options
            AIProcessingPort.QuizItem item = new AIProcessingPort.QuizItem(
                "Exercício é importante?",
                List.of("Sim", "Não"),
                "Sim"
            );

            AIProcessingPort.AIProcessingResult result = new AIProcessingPort.AIProcessingResult(
                "Título",
                "Intro",
                List.of(),
                "Conclusão",
                List.of(item),
                LocalDateTime.now()
            );

            // Act
            ArticleResponse response = mapper.toArticleResponse(result, article);

            // Assert
            Quiz quiz = response.quizzes().getFirst();
            assertEquals(2, quiz.options().size());
        }
    }

    @Nested
    @DisplayName("4. Resposta de Erro - Not Found")
    class NotFoundResponseTests {

        @Test
        @DisplayName("Deve criar resposta de artigo não encontrado")
        void shouldCreateNotFoundResponse() {
            // Act
            ArticleResponse response = mapper.toNotFoundResponse();

            // Assert
            assertNotNull(response);
            assertEquals("Artigo Não Encontrado", response.title());
            assertEquals("Não foi possível encontrar artigos sobre este tema.", response.introduction());
            assertEquals("Por favor, tente reformular sua pergunta.", response.conclusion());
            assertNull(response.sourceLink());
            assertNotNull(response.timestamp());
        }

        @Test
        @DisplayName("Deve criar resposta not found sem recommendations e quizzes")
        void shouldCreateNotFoundResponseWithEmptyLists() {
            // Act
            ArticleResponse response = mapper.toNotFoundResponse();

            // Assert
            assertNotNull(response.recommendations());
            assertTrue(response.recommendations().isEmpty());
            assertNotNull(response.quizzes());
            assertTrue(response.quizzes().isEmpty());
        }

        @Test
        @DisplayName("Deve incluir timestamp atual na resposta not found")
        void shouldIncludeCurrentTimestampInNotFoundResponse() {
            // Arrange
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            // Act
            ArticleResponse response = mapper.toNotFoundResponse();

            // Assert
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            assertTrue(response.timestamp().isAfter(before));
            assertTrue(response.timestamp().isBefore(after));
        }
    }

    @Nested
    @DisplayName("5. Resposta de Erro Genérica")
    class ErrorResponseTests {

        @Test
        @DisplayName("Deve criar resposta de erro com mensagem customizada")
        void shouldCreateErrorResponseWithCustomMessage() {
            // Arrange
            String errorMessage = "Falha ao processar artigo com IA";

            // Act
            ArticleResponse response = mapper.toErrorResponse(errorMessage);

            // Assert
            assertNotNull(response);
            assertEquals("Erro no Processamento", response.title());
            assertEquals(errorMessage, response.introduction());
            assertEquals("Por favor, tente novamente.", response.conclusion());
            assertNull(response.sourceLink());
        }

        @Test
        @DisplayName("Deve criar resposta de erro sem recommendations e quizzes")
        void shouldCreateErrorResponseWithEmptyLists() {
            // Act
            ArticleResponse response = mapper.toErrorResponse("Erro de conexão");

            // Assert
            assertNotNull(response.recommendations());
            assertTrue(response.recommendations().isEmpty());
            assertNotNull(response.quizzes());
            assertTrue(response.quizzes().isEmpty());
        }

        @Test
        @DisplayName("Deve incluir timestamp atual na resposta de erro")
        void shouldIncludeCurrentTimestampInErrorResponse() {
            // Arrange
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            // Act
            ArticleResponse response = mapper.toErrorResponse("Timeout");

            // Assert
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            assertTrue(response.timestamp().isAfter(before));
            assertTrue(response.timestamp().isBefore(after));
        }

        @Test
        @DisplayName("Deve aceitar diferentes mensagens de erro")
        void shouldAcceptDifferentErrorMessages() {
            // Act
            ArticleResponse response1 = mapper.toErrorResponse("Erro de rede");
            ArticleResponse response2 = mapper.toErrorResponse("API indisponível");

            // Assert
            assertEquals("Erro de rede", response1.introduction());
            assertEquals("API indisponível", response2.introduction());
        }
    }

    @Nested
    @DisplayName("6. Validação de Imutabilidade")
    class ImmutabilityTests {

        @Test
        @DisplayName("Deve criar lista imutável de recommendations")
        void shouldCreateImmutableRecommendationsList() {
            // Act
            ArticleResponse response = mapper.toArticleResponse(aiResult, article);

            // Assert
            assertThrows(
                UnsupportedOperationException.class,
                () -> response.recommendations().add(new Recommendation("New", "New", List.of()))
            );
        }

        @Test
        @DisplayName("Deve criar lista imutável de quizzes")
        void shouldCreateImmutableQuizzesList() {
            // Act
            ArticleResponse response = mapper.toArticleResponse(aiResult, article);

            // Assert
            assertThrows(
                UnsupportedOperationException.class,
                () -> response.quizzes().add(new Quiz("Q?", List.of("A"), "A"))
            );
        }
    }

    @Nested
    @DisplayName("7. Testes de Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Deve mapear corretamente artigo com URL muito longa")
        void shouldMapArticleWithVeryLongUrl() {
            // Arrange
            String longUrl = "https://cremesp.org.br/" + "a".repeat(500);
            MedicalArticle articleWithLongUrl = new MedicalArticle(VALID_CONTENT, longUrl);

            // Act
            ArticleResponse response = mapper.toArticleResponse(aiResult, articleWithLongUrl);

            // Assert
            assertEquals(longUrl, response.sourceLink());
        }

        @Test
        @DisplayName("Deve mapear corretamente com múltiplas recommendations")
        void shouldMapWithManyRecommendations() {
            // Arrange
            List<AIProcessingPort.RecommendationItem> manyRecs = List.of(
                new AIProcessingPort.RecommendationItem("Cat1", "Desc1", List.of("T1")),
                new AIProcessingPort.RecommendationItem("Cat2", "Desc2", List.of("T2")),
                new AIProcessingPort.RecommendationItem("Cat3", "Desc3", List.of("T3")),
                new AIProcessingPort.RecommendationItem("Cat4", "Desc4", List.of("T4")),
                new AIProcessingPort.RecommendationItem("Cat5", "Desc5", List.of("T5"))
            );

            AIProcessingPort.AIProcessingResult result = new AIProcessingPort.AIProcessingResult(
                "Título", "Intro", manyRecs, "Conclusão", List.of(), LocalDateTime.now()
            );

            // Act
            ArticleResponse response = mapper.toArticleResponse(result, article);

            // Assert
            assertEquals(5, response.recommendations().size());
        }

        @Test
        @DisplayName("Deve mapear corretamente com múltiplos quizzes")
        void shouldMapWithManyQuizzes() {
            // Arrange
            List<AIProcessingPort.QuizItem> manyQuizzes = List.of(
                new AIProcessingPort.QuizItem("Q1?", List.of("A", "B"), "A"),
                new AIProcessingPort.QuizItem("Q2?", List.of("C", "D"), "C"),
                new AIProcessingPort.QuizItem("Q3?", List.of("E", "F"), "E"),
                new AIProcessingPort.QuizItem("Q4?", List.of("G", "H"), "G")
            );

            AIProcessingPort.AIProcessingResult result = new AIProcessingPort.AIProcessingResult(
                "Título", "Intro", List.of(), "Conclusão", manyQuizzes, LocalDateTime.now()
            );

            // Act
            ArticleResponse response = mapper.toArticleResponse(result, article);

            // Assert
            assertEquals(4, response.quizzes().size());
        }
    }
}
