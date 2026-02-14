package com.fiap.brain.health.api.exception;

import com.fiap.brain.health.api.dto.error.ProblemDetail;
import com.fiap.brain.health.domain.exception.*;
import com.fiap.brain.health.domain.port.AIProcessingPort;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("APIExceptionHandler - Testes Unitários")
class APIExceptionHandlerTest {

    @InjectMocks
    private APIExceptionHandler exceptionHandler;

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Nested
    @DisplayName("Exception Handlers - 4xx Client Errors")
    class ClientErrorHandlers {

        @Test
        @DisplayName("Deve retornar 400 para HttpMessageNotReadableException")
        void shouldReturn400ForHttpMessageNotReadableException() {
            // Arrange
            HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleMessageNotReadable(
                    exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isInstanceOf(ProblemDetail.class);

            ProblemDetail problem = response.getBody();
            assertThat(problem.status()).isEqualTo(400);
            assertThat(problem.title()).isEqualTo("Malformed JSON Request");
            assertThat(problem.detail()).isEqualTo("Malformed JSON request");
        }

        @Test
        @DisplayName("Deve retornar 415 para HttpMediaTypeNotSupportedException")
        void shouldReturn415ForHttpMediaTypeNotSupportedException() {
            // Arrange
            HttpMediaTypeNotSupportedException exception = mock(HttpMediaTypeNotSupportedException.class);

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleUnsupportedMediaType(
                    exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            assertThat(response.getBody()).isInstanceOf(ProblemDetail.class);

            ProblemDetail problem = response.getBody();
            assertThat(problem.status()).isEqualTo(415);
            assertThat(problem.title()).isEqualTo("Unsupported Media Type");
        }

        @Test
        @DisplayName("Deve retornar 400 para MethodArgumentNotValidException com erros de validação")
        void shouldReturn400ForMethodArgumentNotValidExceptionWithValidationErrors() {
            // Arrange
            BindingResult bindingResult = mock(BindingResult.class);
            FieldError fieldError1 = new FieldError("request", "userId", "não pode ser nulo");
            FieldError fieldError2 = new FieldError("request", "title", "não pode estar em branco");

            when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(exception.getMessage()).thenReturn("Validation failed");

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleMethodArgumentNotValid(
                    exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isInstanceOf(ProblemDetail.class);

            ProblemDetail problem = response.getBody();
            assertThat(problem.status()).isEqualTo(400);
            assertThat(problem.title()).isEqualTo("Invalid Request Parameters");
            assertThat(problem.detail()).contains("Request validation failed");
        }

        @Test
        @DisplayName("Deve retornar 400 para IllegalArgumentException")
        void shouldReturn400ForIllegalArgumentException() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("userId is required");

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleIllegalArgument(
                    exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().detail()).isEqualTo("userId is required");
        }

        @Test
        @DisplayName("Deve retornar 404 para ArticleNotFoundException")
        void shouldReturn404ForArticleNotFoundException() {
            // Arrange
            ArticleNotFoundException exception = new ArticleNotFoundException("Artigo não encontrado");

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleArticleNotFound(
                    exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(404);
            assertThat(response.getBody().title()).isEqualTo("Medical Article Not Found");
            assertThat(response.getBody().detail()).isEqualTo("Medical Article not found with identifier: Artigo não encontrado");
        }
    }

    @Nested
    @DisplayName("Exception Handlers - 5xx Server Errors")
    class ServerErrorHandlers {

        @Test
        @DisplayName("Deve retornar 500 para Exception genérica")
        void shouldReturn500ForGenericException() {
            // Arrange
            Exception exception = new Exception("Unexpected error occurred");

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleGenericException(
                    exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(500);
            assertThat(response.getBody().title()).isEqualTo("Internal Server Error");
        }

        @Test
        @DisplayName("Deve retornar 500 para AIProcessingException")
        void shouldReturn500ForAIProcessingException() {
            // Arrange
            AIProcessingPort.AIProcessingException exception =
                    new AIProcessingPort.AIProcessingException("AI service unavailable");

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleAIProcessing(
                    exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(500);
            assertThat(response.getBody().detail()).contains("AI service unavailable");
        }

        @Test
        @DisplayName("Deve retornar 500 para ArticleSearchException")
        void shouldReturn500ForArticleSearchException() {
            // Arrange
            ArticleSearchException exception = new ArticleSearchException("Search service error");

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleArticleSearch(
                    exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().detail()).contains("Search service error");
        }
    }

    @Nested
    @DisplayName("ProblemDetail Structure")
    class ProblemDetailStructure {

        @Test
        @DisplayName("Deve incluir timestamp em todas as respostas")
        void shouldIncludeTimestampInAllResponses() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("Test error");

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleIllegalArgument(
                    exception, request);

            // Assert
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().timestamp()).isNotNull();
        }

        @Test
        @DisplayName("Deve incluir traceId em todas as respostas")
        void shouldIncludeTraceIdInAllResponses() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("Test error");

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleIllegalArgument(
                    exception, request);

            // Assert
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().traceId()).isNotNull();
            assertThat(response.getBody().traceId()).isNotEmpty();
        }

        @Test
        @DisplayName("Deve incluir instance (URI) em todas as respostas")
        void shouldIncludeInstanceInAllResponses() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("Test error");

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleIllegalArgument(
                    exception, request);

            // Assert
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().instance()).isNotNull();
        }

        @Test
        @DisplayName("Deve usar type URI padrão RFC 7807")
        void shouldUseRFC7807TypeURI() {
            // Arrange
            ArticleNotFoundException exception = new ArticleNotFoundException("Not found");

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleArticleNotFound(
                    exception, request);

            // Assert
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().type()).isNotNull();
            assertThat(response.getBody().type()).contains("brain-health.fiap.com/problems");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Deve tratar exception com mensagem nula")
        void shouldHandleExceptionWithNullMessage() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException();

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleIllegalArgument(
                    exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Deve tratar exception com mensagem vazia")
        void shouldHandleExceptionWithEmptyMessage() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("");

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleIllegalArgument(
                    exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Deve tratar mensagem de erro muito longa")
        void shouldHandleVeryLongErrorMessage() {
            // Arrange
            String longMessage = "Error: " + "x".repeat(1000);
            IllegalArgumentException exception = new IllegalArgumentException(longMessage);

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleIllegalArgument(
                    exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().detail()).contains("Error:");
        }
    }

    @Nested
    @DisplayName("Content Negotiation")
    class ContentNegotiationTests {

        @Test
        @DisplayName("Deve retornar JSON para HttpMediaTypeNotSupportedException")
        void shouldReturnJSONForMediaTypeError() {
            // Arrange
            HttpMediaTypeNotSupportedException exception = mock(HttpMediaTypeNotSupportedException.class);

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleUnsupportedMediaType(
                    exception, request);

            // Assert
            assertThat(response.getBody()).isInstanceOf(ProblemDetail.class);
        }

        @Test
        @DisplayName("Deve retornar JSON para HttpMessageNotReadableException")
        void shouldReturnJSONForNotReadableError() {
            // Arrange
            HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleMessageNotReadable(
                    exception, request);

            // Assert
            assertThat(response.getBody()).isInstanceOf(ProblemDetail.class);
        }
    }

    @Nested
    @DisplayName("Validation Error Details")
    class ValidationErrorDetails {

        @Test
        @DisplayName("Deve incluir detalhes de todos os erros de validação")
        void shouldIncludeAllValidationErrorDetails() {
            // Arrange
            BindingResult bindingResult = mock(BindingResult.class);
            FieldError error1 = new FieldError("request", "userId", "not null");
            FieldError error2 = new FieldError("request", "title", "not blank");
            FieldError error3 = new FieldError("request", "category", "invalid");

            when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2, error3));

            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(exception.getMessage()).thenReturn("Validation failed for multiple fields");

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleMethodArgumentNotValid(
                    exception, request);

            // Assert
            ProblemDetail problem = response.getBody();
            assertThat(problem).isNotNull();
            assertThat(problem.detail()).contains("validation");
        }

        @Test
        @DisplayName("Deve formatar erros de validação corretamente")
        void shouldFormatValidationErrorsCorrectly() {
            // Arrange
            BindingResult bindingResult = mock(BindingResult.class);
            FieldError error = new FieldError("request", "email", "must be valid");
            when(bindingResult.getFieldErrors()).thenReturn(List.of(error));

            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(exception.getMessage()).thenReturn("Email validation failed");

            // Act
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleMethodArgumentNotValid(
                    exception, request);

            // Assert
            assertThat(response.getBody()).isInstanceOf(ProblemDetail.class);
            ProblemDetail problem = response.getBody();
            assertThat(problem.detail()).contains("validation");
        }
    }
}
