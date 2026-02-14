package com.fiap.brain.health.application.usecase;

import com.fiap.brain.health.api.dto.kafka.BrainHealthRequestMessage;
import com.fiap.brain.health.api.dto.kafka.BrainHealthResponseMessage;
import com.fiap.brain.health.api.dto.response.ArticleResponse;
import com.fiap.brain.health.application.mapper.ArticleResponseMapper;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessKafkaMessageUseCase - Testes Unitários")
class ProcessKafkaMessageUseCaseTest {

    @Mock
    private MedicalArticleRepositoryPort articleRepository;

    @Mock
    private AIProcessingPort aiProcessing;

    @Mock
    private ArticleResponseMapper responseMapper;

    @InjectMocks
    private ProcessKafkaMessageUseCase useCase;

    private BrainHealthRequestMessage validRequest;
    private MedicalArticle validArticle;
    private AIProcessingPort.AIProcessingResult aiResult;
    private ArticleResponse articleResponse;

    private static final Long USER_ID = 1L;
    private static final Long GOAL_ID = 1L;
    private static final String MESSAGE_ID = "msg-123";
    private static final String CORRELATION_ID = "corr-456";
    private static final String TITLE = "Benefícios da Caminhada";
    private static final String VALID_CONTENT = "A".repeat(150);
    private static final String VALID_URL = "https://cremesp.org.br/article/123";

    @BeforeEach
    void setUp() {
        // Setup request message
        validRequest = BrainHealthRequestMessage.builder()
                .goalId(GOAL_ID)
                .userId(USER_ID)
                .category("SAUDE_FISICA")
                .title(TITLE)
                .description("Artigo sobre caminhada")
                .messageId(MESSAGE_ID)
                .correlationId(CORRELATION_ID)
                .requestedAt(LocalDateTime.now())
                .build();

        // Setup article
        validArticle = new MedicalArticle(VALID_CONTENT, VALID_URL);

        // Setup AI result
        aiResult = new AIProcessingPort.AIProcessingResult(
            "Benefícios da Caminhada",
            "A caminhada é uma atividade benéfica.",
            List.of(
                new AIProcessingPort.RecommendationItem(
                    "Frequência",
                    "Caminhe 30 minutos/dia",
                    List.of("Comece devagar")
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
            LocalDateTime.now()
        );

        // Setup article response
        articleResponse = new ArticleResponse(
            "Benefícios da Caminhada",
            "A caminhada é uma atividade benéfica.",
            List.of(),
            "Pratique regularmente.",
            List.of(),
            VALID_URL,
            LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("1. Cenários de Sucesso")
    class SuccessScenarios {

        @Test
        @DisplayName("Deve processar mensagem Kafka com sucesso")
        void shouldProcessKafkaMessageSuccessfully() {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(validArticle));
            when(aiProcessing.processArticle(TITLE, validArticle)).thenReturn(aiResult);
            when(responseMapper.toArticleResponse(aiResult, validArticle)).thenReturn(articleResponse);

            // Act
            BrainHealthResponseMessage response = useCase.process(validRequest);

            // Assert
            assertNotNull(response, "Response não deve ser nulo");
            assertEquals(BrainHealthResponseMessage.ProcessingStatus.SUCCESS, response.status());
            assertEquals(USER_ID, response.userId());
            assertEquals(CORRELATION_ID, response.correlationId());
            assertNotNull(response.messageId());
            assertNotNull(response.articleResponse());
            assertNull(response.errorMessage());
            assertNotNull(response.processedAt());

            // Verify interactions
            verify(articleRepository).findByTopic(TITLE);
            verify(aiProcessing).processArticle(TITLE, validArticle);
            verify(responseMapper).toArticleResponse(aiResult, validArticle);
        }

        @Test
        @DisplayName("Deve gerar novo messageId diferente do request")
        void shouldGenerateNewMessageId() {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(validArticle));
            when(aiProcessing.processArticle(TITLE, validArticle)).thenReturn(aiResult);
            when(responseMapper.toArticleResponse(aiResult, validArticle)).thenReturn(articleResponse);

            // Act
            BrainHealthResponseMessage response = useCase.process(validRequest);

            // Assert
            assertNotNull(response.messageId());
            assertNotEquals(MESSAGE_ID, response.messageId(), "MessageId deve ser diferente do request");
        }

        @Test
        @DisplayName("Deve manter correlationId do request na response")
        void shouldMaintainCorrelationId() {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(validArticle));
            when(aiProcessing.processArticle(TITLE, validArticle)).thenReturn(aiResult);
            when(responseMapper.toArticleResponse(aiResult, validArticle)).thenReturn(articleResponse);

            // Act
            BrainHealthResponseMessage response = useCase.process(validRequest);

            // Assert
            assertEquals(CORRELATION_ID, response.correlationId());
        }

        @Test
        @DisplayName("Deve incluir ArticleResponse completo no sucesso")
        void shouldIncludeCompleteArticleResponseOnSuccess() {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(validArticle));
            when(aiProcessing.processArticle(TITLE, validArticle)).thenReturn(aiResult);
            when(responseMapper.toArticleResponse(aiResult, validArticle)).thenReturn(articleResponse);

            // Act
            BrainHealthResponseMessage response = useCase.process(validRequest);

            // Assert
            assertNotNull(response.articleResponse());
            assertEquals("Benefícios da Caminhada", response.articleResponse().title());
        }
    }

    @Nested
    @DisplayName("2. Cenários de Falha - ArticleNotFoundException")
    class ArticleNotFoundScenarios {

        @Test
        @DisplayName("Deve retornar response FAILED quando artigo não é encontrado")
        void shouldReturnFailedResponseWhenArticleNotFound() {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.empty());

            // Act
            BrainHealthResponseMessage response = useCase.process(validRequest);

            // Assert
            assertNotNull(response);
            assertEquals(BrainHealthResponseMessage.ProcessingStatus.FAILED, response.status());
            assertEquals(USER_ID, response.userId());
            assertEquals(CORRELATION_ID, response.correlationId());
            assertNotNull(response.errorMessage());
            assertTrue(response.errorMessage().contains("Article not found"));
            assertNull(response.articleResponse());

            // Verify interactions
            verify(articleRepository).findByTopic(TITLE);
            verify(aiProcessing, never()).processArticle(any(), any());
            verify(responseMapper, never()).toArticleResponse(any(), any());
        }

        @Test
        @DisplayName("Deve incluir mensagem de erro específica para artigo não encontrado")
        void shouldIncludeSpecificErrorMessageForArticleNotFound() {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.empty());

            // Act
            BrainHealthResponseMessage response = useCase.process(validRequest);

            // Assert
            assertNotNull(response.errorMessage());
            assertTrue(response.errorMessage().contains("Article not found"));
        }
    }

    @Nested
    @DisplayName("3. Cenários de Falha - InsufficientContentException")
    class InsufficientContentScenarios {

        @Test
        @DisplayName("Deve retornar response FAILED quando conteúdo é insuficiente")
        void shouldReturnFailedResponseWhenContentInsufficient() {
            // Arrange
            String shortContent = "Conteúdo curto"; // Menos de 100 caracteres
            MedicalArticle shortArticle = new MedicalArticle(shortContent, VALID_URL);
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(shortArticle));

            // Act
            BrainHealthResponseMessage response = useCase.process(validRequest);

            // Assert
            assertNotNull(response);
            assertEquals(BrainHealthResponseMessage.ProcessingStatus.FAILED, response.status());
            assertNotNull(response.errorMessage());
            assertTrue(response.errorMessage().contains("Article content too short") ||
                      response.errorMessage().contains("INSUFFICIENT_CONTENT"));
            assertNull(response.articleResponse());

            // Verify interactions
            verify(articleRepository).findByTopic(TITLE);
            verify(aiProcessing, never()).processArticle(any(), any());
        }

        @Test
        @DisplayName("Deve incluir detalhes do erro de conteúdo insuficiente")
        void shouldIncludeInsufficientContentErrorDetails() {
            // Arrange
            String shortContent = "A".repeat(50);
            MedicalArticle shortArticle = new MedicalArticle(shortContent, VALID_URL);
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(shortArticle));

            // Act
            BrainHealthResponseMessage response = useCase.process(validRequest);

            // Assert
            assertNotNull(response.errorMessage());
            assertTrue(response.errorMessage().contains("50") ||
                      response.errorMessage().contains("100"));
        }
    }

    @Nested
    @DisplayName("4. Cenários de Falha - AIProcessingException")
    class AIProcessingExceptionScenarios {

        @Test
        @DisplayName("Deve retornar response FAILED quando AI processing falha")
        void shouldReturnFailedResponseWhenAIProcessingFails() {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(validArticle));
            when(aiProcessing.processArticle(TITLE, validArticle))
                .thenThrow(new AIProcessingPort.AIProcessingException("OpenAI API error"));

            // Act
            BrainHealthResponseMessage response = useCase.process(validRequest);

            // Assert
            assertNotNull(response);
            assertEquals(BrainHealthResponseMessage.ProcessingStatus.FAILED, response.status());
            assertNotNull(response.errorMessage());
            assertTrue(response.errorMessage().contains("AI processing error"));
            assertNull(response.articleResponse());

            // Verify interactions
            verify(articleRepository).findByTopic(TITLE);
            verify(aiProcessing).processArticle(TITLE, validArticle);
            verify(responseMapper, never()).toArticleResponse(any(), any());
        }

        @Test
        @DisplayName("Deve incluir mensagem original do erro de AI processing")
        void shouldIncludeOriginalAIProcessingErrorMessage() {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(validArticle));
            when(aiProcessing.processArticle(TITLE, validArticle))
                .thenThrow(new AIProcessingPort.AIProcessingException("Rate limit exceeded"));

            // Act
            BrainHealthResponseMessage response = useCase.process(validRequest);

            // Assert
            assertTrue(response.errorMessage().contains("Rate limit exceeded"));
        }
    }

    @Nested
    @DisplayName("5. Cenários de Falha - Exceções Inesperadas")
    class UnexpectedExceptionScenarios {

        @Test
        @DisplayName("Deve retornar response FAILED quando ocorre exceção inesperada")
        void shouldReturnFailedResponseWhenUnexpectedExceptionOccurs() {
            // Arrange
            when(articleRepository.findByTopic(TITLE))
                .thenThrow(new RuntimeException("Database connection lost"));

            // Act
            BrainHealthResponseMessage response = useCase.process(validRequest);

            // Assert
            assertNotNull(response);
            assertEquals(BrainHealthResponseMessage.ProcessingStatus.FAILED, response.status());
            assertNotNull(response.errorMessage());
            assertTrue(response.errorMessage().contains("Unexpected error"));
            assertNull(response.articleResponse());
        }

        @Test
        @DisplayName("Deve incluir mensagem da exceção inesperada")
        void shouldIncludeUnexpectedExceptionMessage() {
            // Arrange
            when(articleRepository.findByTopic(TITLE))
                .thenThrow(new NullPointerException("Unexpected null value"));

            // Act
            BrainHealthResponseMessage response = useCase.process(validRequest);

            // Assert
            assertTrue(response.errorMessage().contains("Unexpected null value"));
        }
    }

    @Nested
    @DisplayName("6. Validação de Campos da Response")
    class ResponseFieldValidationTests {

        @Test
        @DisplayName("Deve sempre incluir userId na response")
        void shouldAlwaysIncludeUserId() {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.empty());

            // Act
            BrainHealthResponseMessage response = useCase.process(validRequest);

            // Assert
            assertEquals(USER_ID, response.userId());
        }

        @Test
        @DisplayName("Deve sempre incluir correlationId na response")
        void shouldAlwaysIncludeCorrelationId() {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.empty());

            // Act
            BrainHealthResponseMessage response = useCase.process(validRequest);

            // Assert
            assertEquals(CORRELATION_ID, response.correlationId());
        }

        @Test
        @DisplayName("Deve sempre incluir processedAt timestamp")
        void shouldAlwaysIncludeProcessedAt() {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.empty());
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            // Act
            BrainHealthResponseMessage response = useCase.process(validRequest);

            // Assert
            assertNotNull(response.processedAt());
            assertTrue(response.processedAt().isAfter(before));
        }

        @Test
        @DisplayName("Deve incluir errorMessage null em caso de sucesso")
        void shouldIncludeNullErrorMessageOnSuccess() {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(validArticle));
            when(aiProcessing.processArticle(TITLE, validArticle)).thenReturn(aiResult);
            when(responseMapper.toArticleResponse(aiResult, validArticle)).thenReturn(articleResponse);

            // Act
            BrainHealthResponseMessage response = useCase.process(validRequest);

            // Assert
            assertNull(response.errorMessage());
        }

        @Test
        @DisplayName("Deve incluir articleResponse null em caso de falha")
        void shouldIncludeNullArticleResponseOnFailure() {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.empty());

            // Act
            BrainHealthResponseMessage response = useCase.process(validRequest);

            // Assert
            assertNull(response.articleResponse());
        }
    }

    @Nested
    @DisplayName("7. Testes de Integração entre Componentes")
    class ComponentIntegrationTests {

        @Test
        @DisplayName("Deve chamar todos os componentes na ordem correta")
        void shouldCallComponentsInCorrectOrder() {
            // Arrange
            when(articleRepository.findByTopic(TITLE)).thenReturn(Optional.of(validArticle));
            when(aiProcessing.processArticle(TITLE, validArticle)).thenReturn(aiResult);
            when(responseMapper.toArticleResponse(aiResult, validArticle)).thenReturn(articleResponse);

            // Act
            useCase.process(validRequest);

            // Assert - Verify order of calls
            var inOrder = inOrder(articleRepository, aiProcessing, responseMapper);
            inOrder.verify(articleRepository).findByTopic(TITLE);
            inOrder.verify(aiProcessing).processArticle(TITLE, validArticle);
            inOrder.verify(responseMapper).toArticleResponse(aiResult, validArticle);
        }

        @Test
        @DisplayName("Deve usar title do request para buscar artigo")
        void shouldUseTitleFromRequestToFindArticle() {
            // Arrange
            String customTitle = "Artigo Customizado";
            BrainHealthRequestMessage customRequest = BrainHealthRequestMessage.builder()
                    .userId(USER_ID)
                    .title(customTitle)
                    .messageId(MESSAGE_ID)
                    .correlationId(CORRELATION_ID)
                    .requestedAt(LocalDateTime.now())
                    .build();

            when(articleRepository.findByTopic(customTitle)).thenReturn(Optional.of(validArticle));
            when(aiProcessing.processArticle(customTitle, validArticle)).thenReturn(aiResult);
            when(responseMapper.toArticleResponse(aiResult, validArticle)).thenReturn(articleResponse);

            // Act
            useCase.process(customRequest);

            // Assert
            verify(articleRepository).findByTopic(customTitle);
            verify(aiProcessing).processArticle(customTitle, validArticle);
        }
    }
}
