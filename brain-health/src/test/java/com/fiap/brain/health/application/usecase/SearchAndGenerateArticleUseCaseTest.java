package com.fiap.brain.health.application.usecase;

import com.fiap.brain.health.domain.exception.ArticleNotFoundException;
import com.fiap.brain.health.domain.exception.InsufficientContentException;
import com.fiap.brain.health.domain.model.MedicalArticle;
import com.fiap.brain.health.domain.port.AIProcessingPort;
import com.fiap.brain.health.domain.port.MedicalArticleRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchAndGenerateArticleUseCase - Testes Unitários")
class SearchAndGenerateArticleUseCaseTest {

    @Mock
    private MedicalArticleRepositoryPort articleRepository;

    @Mock
    private AIProcessingPort aiProcessing;

    @InjectMocks
    private SearchAndGenerateArticleUseCase useCase;

    private static final String QUESTION = "Benefícios da Caminhada";
    private static final String VALID_CONTENT = "A".repeat(150); // Mais de 100 caracteres
    private static final String VALID_URL = "https://cremesp.org.br/article/123";

    private MedicalArticle validArticle;
    private AIProcessingPort.AIProcessingResult aiResult;

    @BeforeEach
    void setUp() {
        validArticle = new MedicalArticle(VALID_CONTENT, VALID_URL);

        aiResult = new AIProcessingPort.AIProcessingResult(
            "Benefícios da Caminhada para Saúde",
            "A caminhada é uma atividade física acessível e benéfica.",
            List.of(
                new AIProcessingPort.RecommendationItem(
                    "Frequência",
                    "Caminhe pelo menos 30 minutos por dia",
                    List.of("Comece devagar", "Aumente gradualmente")
                )
            ),
            "A caminhada regular traz inúmeros benefícios.",
            List.of(
                new AIProcessingPort.QuizItem(
                    "Qual a duração mínima recomendada?",
                    List.of("15 minutos", "30 minutos", "60 minutos"),
                    "30 minutos"
                )
            ),
            LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("1. Cenários de Sucesso")
    class SuccessScenarios {

        @Test
        @DisplayName("Deve executar busca e geração de artigo com sucesso")
        void shouldExecuteSuccessfully() {
            // Arrange
            when(articleRepository.findByTopic(QUESTION)).thenReturn(Optional.of(validArticle));
            when(aiProcessing.processArticle(QUESTION, validArticle)).thenReturn(aiResult);

            // Act
            AIProcessingPort.AIProcessingResult result = useCase.execute(QUESTION);

            // Assert
            assertNotNull(result, "Resultado não deve ser nulo");
            assertEquals("Benefícios da Caminhada para Saúde", result.title());
            assertNotNull(result.introduction());
            assertNotNull(result.recommendations());
            assertFalse(result.recommendations().isEmpty());
            assertNotNull(result.conclusion());
            assertNotNull(result.quizzes());

            // Verify interactions
            verify(articleRepository, times(1)).findByTopic(QUESTION);
            verify(aiProcessing, times(1)).processArticle(QUESTION, validArticle);
        }

        @Test
        @DisplayName("Deve processar artigo de fonte confiável com sucesso")
        void shouldProcessTrustedSourceArticleSuccessfully() {
            // Arrange
            MedicalArticle trustedArticle = new MedicalArticle(VALID_CONTENT, "https://pubmed.gov/article/123");
            when(articleRepository.findByTopic(QUESTION)).thenReturn(Optional.of(trustedArticle));
            when(aiProcessing.processArticle(QUESTION, trustedArticle)).thenReturn(aiResult);

            // Act
            AIProcessingPort.AIProcessingResult result = useCase.execute(QUESTION);

            // Assert
            assertNotNull(result);
            assertTrue(trustedArticle.isFromTrustedSource(), "Artigo deve ser de fonte confiável");
            verify(articleRepository).findByTopic(QUESTION);
            verify(aiProcessing).processArticle(QUESTION, trustedArticle);
        }

        @Test
        @DisplayName("Deve processar artigo de fonte não confiável emitindo warning")
        void shouldProcessUntrustedSourceArticleWithWarning() {
            // Arrange
            String untrustedUrl = "https://blog.exemplo.com/artigo";
            MedicalArticle untrustedArticle = new MedicalArticle(VALID_CONTENT, untrustedUrl);
            when(articleRepository.findByTopic(QUESTION)).thenReturn(Optional.of(untrustedArticle));
            when(aiProcessing.processArticle(QUESTION, untrustedArticle)).thenReturn(aiResult);

            // Act
            AIProcessingPort.AIProcessingResult result = useCase.execute(QUESTION);

            // Assert
            assertNotNull(result, "Deve processar mesmo sendo fonte não confiável");
            assertFalse(untrustedArticle.isFromTrustedSource(), "Artigo não deve ser de fonte confiável");
            verify(articleRepository).findByTopic(QUESTION);
            verify(aiProcessing).processArticle(QUESTION, untrustedArticle);
        }

        @Test
        @DisplayName("Deve processar artigo com conteúdo exatamente no limite mínimo")
        void shouldProcessArticleWithExactMinimumContent() {
            // Arrange
            String minimumContent = "A".repeat(100); // Exatamente 100 caracteres
            MedicalArticle articleWithMinimumContent = new MedicalArticle(minimumContent, VALID_URL);
            when(articleRepository.findByTopic(QUESTION)).thenReturn(Optional.of(articleWithMinimumContent));
            when(aiProcessing.processArticle(QUESTION, articleWithMinimumContent)).thenReturn(aiResult);

            // Act
            AIProcessingPort.AIProcessingResult result = useCase.execute(QUESTION);

            // Assert
            assertNotNull(result);
            assertEquals(100, articleWithMinimumContent.getContentLength());
            verify(aiProcessing).processArticle(QUESTION, articleWithMinimumContent);
        }
    }

    @Nested
    @DisplayName("2. Cenários de Falha - ArticleNotFoundException")
    class ArticleNotFoundScenarios {

        @Test
        @DisplayName("Deve lançar ArticleNotFoundException quando artigo não existe")
        void shouldThrowArticleNotFoundExceptionWhenArticleDoesNotExist() {
            // Arrange
            when(articleRepository.findByTopic(QUESTION)).thenReturn(Optional.empty());

            // Act & Assert
            ArticleNotFoundException exception = assertThrows(
                ArticleNotFoundException.class,
                () -> useCase.execute(QUESTION),
                "Deve lançar ArticleNotFoundException"
            );

            assertNotNull(exception.getMessage());
            verify(articleRepository).findByTopic(QUESTION);
            verify(aiProcessing, never()).processArticle(any(), any());
        }

        @Test
        @DisplayName("Deve lançar ArticleNotFoundException com tópico correto")
        void shouldThrowArticleNotFoundExceptionWithCorrectTopic() {
            // Arrange
            String specificQuestion = "Artigo Inexistente";
            when(articleRepository.findByTopic(specificQuestion)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                ArticleNotFoundException.class,
                () -> useCase.execute(specificQuestion)
            );

            verify(articleRepository).findByTopic(specificQuestion);
        }
    }

    @Nested
    @DisplayName("3. Cenários de Falha - InsufficientContentException")
    class InsufficientContentScenarios {

        @Test
        @DisplayName("Deve lançar InsufficientContentException quando conteúdo tem menos de 100 caracteres")
        void shouldThrowInsufficientContentExceptionWhenContentTooShort() {
            // Arrange
            String shortContent = "Conteúdo muito curto"; // Menos de 100 caracteres
            MedicalArticle shortArticle = new MedicalArticle(shortContent, VALID_URL);
            when(articleRepository.findByTopic(QUESTION)).thenReturn(Optional.of(shortArticle));

            // Act & Assert
            InsufficientContentException exception = assertThrows(
                InsufficientContentException.class,
                () -> useCase.execute(QUESTION),
                "Deve lançar InsufficientContentException"
            );

            assertEquals(shortContent.length(), exception.getActualLength());
            assertEquals(100, exception.getMinimumLength());
            assertTrue(exception.getMessage().contains("Article content too short"));

            verify(articleRepository).findByTopic(QUESTION);
            verify(aiProcessing, never()).processArticle(any(), any());
        }

        @Test
        @DisplayName("Deve lançar InsufficientContentException com 99 caracteres")
        void shouldThrowInsufficientContentExceptionWith99Characters() {
            // Arrange
            String content99Chars = "A".repeat(99);
            MedicalArticle article = new MedicalArticle(content99Chars, VALID_URL);
            when(articleRepository.findByTopic(QUESTION)).thenReturn(Optional.of(article));

            // Act & Assert
            InsufficientContentException exception = assertThrows(
                InsufficientContentException.class,
                () -> useCase.execute(QUESTION)
            );

            assertEquals(99, exception.getActualLength());
            assertEquals(100, exception.getMinimumLength());
        }
    }

    @Nested
    @DisplayName("4. Cenários de Falha - AIProcessingException")
    class AIProcessingExceptionScenarios {

        @Test
        @DisplayName("Deve lançar AIProcessingException quando processamento de IA falha")
        void shouldThrowAIProcessingExceptionWhenAIFails() {
            // Arrange
            when(articleRepository.findByTopic(QUESTION)).thenReturn(Optional.of(validArticle));
            when(aiProcessing.processArticle(QUESTION, validArticle))
                .thenThrow(new AIProcessingPort.AIProcessingException("API Error"));

            // Act & Assert
            AIProcessingPort.AIProcessingException exception = assertThrows(
                AIProcessingPort.AIProcessingException.class,
                () -> useCase.execute(QUESTION),
                "Deve propagar AIProcessingException"
            );

            assertEquals("API Error", exception.getMessage());
            verify(articleRepository).findByTopic(QUESTION);
            verify(aiProcessing).processArticle(QUESTION, validArticle);
        }

        @Test
        @DisplayName("Deve lançar AIProcessingException com causa original")
        void shouldThrowAIProcessingExceptionWithCause() {
            // Arrange
            Exception rootCause = new RuntimeException("OpenAI timeout");
            when(articleRepository.findByTopic(QUESTION)).thenReturn(Optional.of(validArticle));
            when(aiProcessing.processArticle(QUESTION, validArticle))
                .thenThrow(new AIProcessingPort.AIProcessingException("Processing failed", rootCause));

            // Act & Assert
            AIProcessingPort.AIProcessingException exception = assertThrows(
                AIProcessingPort.AIProcessingException.class,
                () -> useCase.execute(QUESTION)
            );

            assertEquals("Processing failed", exception.getMessage());
            assertNotNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("5. Cenários de Falha - Exceções Inesperadas")
    class UnexpectedExceptionScenarios {

        @Test
        @DisplayName("Deve lançar RuntimeException quando ocorre erro inesperado no repositório")
        void shouldThrowRuntimeExceptionWhenRepositoryThrowsUnexpectedException() {
            // Arrange
            when(articleRepository.findByTopic(QUESTION))
                .thenThrow(new RuntimeException("Database connection error"));

            // Act & Assert
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> useCase.execute(QUESTION)
            );

            assertTrue(exception.getMessage().contains("Failed to execute article generation use case"));
            verify(articleRepository).findByTopic(QUESTION);
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando ocorre NullPointerException inesperado")
        void shouldThrowRuntimeExceptionWhenNullPointerOccurs() {
            // Arrange
            when(articleRepository.findByTopic(QUESTION))
                .thenThrow(new NullPointerException("Unexpected null"));

            // Act & Assert
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> useCase.execute(QUESTION)
            );

            assertTrue(exception.getMessage().contains("Failed to execute article generation use case"));
        }
    }

    @Nested
    @DisplayName("6. Validação de Interações")
    class InteractionValidationTests {

        @Test
        @DisplayName("Deve chamar repositório exatamente uma vez")
        void shouldCallRepositoryExactlyOnce() {
            // Arrange
            when(articleRepository.findByTopic(QUESTION)).thenReturn(Optional.of(validArticle));
            when(aiProcessing.processArticle(QUESTION, validArticle)).thenReturn(aiResult);

            // Act
            useCase.execute(QUESTION);

            // Assert
            verify(articleRepository, times(1)).findByTopic(QUESTION);
        }

        @Test
        @DisplayName("Deve chamar AI processing exatamente uma vez em caso de sucesso")
        void shouldCallAIProcessingExactlyOnceOnSuccess() {
            // Arrange
            when(articleRepository.findByTopic(QUESTION)).thenReturn(Optional.of(validArticle));
            when(aiProcessing.processArticle(QUESTION, validArticle)).thenReturn(aiResult);

            // Act
            useCase.execute(QUESTION);

            // Assert
            verify(aiProcessing, times(1)).processArticle(QUESTION, validArticle);
        }

        @Test
        @DisplayName("Não deve chamar AI processing quando artigo não é encontrado")
        void shouldNotCallAIProcessingWhenArticleNotFound() {
            // Arrange
            when(articleRepository.findByTopic(QUESTION)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ArticleNotFoundException.class, () -> useCase.execute(QUESTION));
            verify(aiProcessing, never()).processArticle(any(), any());
        }

        @Test
        @DisplayName("Não deve chamar AI processing quando conteúdo é insuficiente")
        void shouldNotCallAIProcessingWhenContentInsufficient() {
            // Arrange
            MedicalArticle shortArticle = new MedicalArticle("Short", VALID_URL);
            when(articleRepository.findByTopic(QUESTION)).thenReturn(Optional.of(shortArticle));

            // Act & Assert
            assertThrows(InsufficientContentException.class, () -> useCase.execute(QUESTION));
            verify(aiProcessing, never()).processArticle(any(), any());
        }
    }
}
