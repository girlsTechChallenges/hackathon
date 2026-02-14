package com.fiap.user.health.bff.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiErrorMessage - Testes Unitários")
class ApiErrorMessageTest {

    @Test
    @DisplayName("Deve criar ApiErrorMessage com todos os campos")
    void shouldCreateApiErrorMessageWithAllFields() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        ApiErrorMessage.FieldError fieldError = ApiErrorMessage.FieldError.builder()
                .field("email")
                .message("must not be blank")
                .build();

        // Act
        ApiErrorMessage apiError = ApiErrorMessage.builder()
                .timestamp(timestamp)
                .status(400)
                .error("Bad Request")
                .message("Validation failed")
                .path("/api/users")
                .errors(List.of(fieldError))
                .build();

        // Assert
        assertThat(apiError.getTimestamp()).isEqualTo(timestamp);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getPath()).isEqualTo("/api/users");
        assertThat(apiError.getErrors()).hasSize(1);
        assertThat(apiError.getErrors().getFirst().getField()).isEqualTo("email");
        assertThat(apiError.getErrors().getFirst().getMessage()).isEqualTo("must not be blank");
    }

    @Test
    @DisplayName("Deve criar ApiErrorMessage sem erros de campo")
    void shouldCreateApiErrorMessageWithoutFieldErrors() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();

        // Act
        ApiErrorMessage apiError = ApiErrorMessage.builder()
                .timestamp(timestamp)
                .status(404)
                .error("Not Found")
                .message("User not found")
                .path("/api/users/123")
                .build();

        // Assert
        assertThat(apiError.getTimestamp()).isEqualTo(timestamp);
        assertThat(apiError.getStatus()).isEqualTo(404);
        assertThat(apiError.getError()).isEqualTo("Not Found");
        assertThat(apiError.getMessage()).isEqualTo("User not found");
        assertThat(apiError.getPath()).isEqualTo("/api/users/123");
        assertThat(apiError.getErrors()).isNull();
    }

    @Test
    @DisplayName("Deve criar FieldError com builder")
    void shouldCreateFieldErrorWithBuilder() {
        // Act
        ApiErrorMessage.FieldError fieldError = ApiErrorMessage.FieldError.builder()
                .field("nome")
                .message("must not be null")
                .build();

        // Assert
        assertThat(fieldError.getField()).isEqualTo("nome");
        assertThat(fieldError.getMessage()).isEqualTo("must not be null");
    }

    @Test
    @DisplayName("Deve permitir múltiplos erros de campo")
    void shouldAllowMultipleFieldErrors() {
        // Arrange
        ApiErrorMessage.FieldError error1 = ApiErrorMessage.FieldError.builder()
                .field("email")
                .message("must be a valid email")
                .build();

        ApiErrorMessage.FieldError error2 = ApiErrorMessage.FieldError.builder()
                .field("senha")
                .message("must have at least 8 characters")
                .build();

        // Act
        ApiErrorMessage apiError = ApiErrorMessage.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .error("Bad Request")
                .message("Validation failed")
                .path("/api/users")
                .errors(List.of(error1, error2))
                .build();

        // Assert
        assertThat(apiError.getErrors()).hasSize(2);
        assertThat(apiError.getErrors().get(0).getField()).isEqualTo("email");
        assertThat(apiError.getErrors().get(1).getField()).isEqualTo("senha");
    }

    @Test
    @DisplayName("Deve usar construtor sem argumentos")
    void shouldUseNoArgsConstructor() {
        // Act
        ApiErrorMessage apiError = new ApiErrorMessage();

        // Assert
        assertThat(apiError).isNotNull();
    }

    @Test
    @DisplayName("Deve usar construtor com todos os argumentos")
    void shouldUseAllArgsConstructor() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        List<ApiErrorMessage.FieldError> errors = List.of(
                new ApiErrorMessage.FieldError("field", "message")
        );

        // Act
        ApiErrorMessage apiError = new ApiErrorMessage(
                timestamp,
                404,
                "Not Found",
                "Resource not found",
                "/api/test",
                errors
        );

        // Assert
        assertThat(apiError.getStatus()).isEqualTo(404);
        assertThat(apiError.getMessage()).isEqualTo("Resource not found");
    }

    @Test
    @DisplayName("Deve permitir setters")
    void shouldAllowSetters() {
        // Arrange
        ApiErrorMessage apiError = new ApiErrorMessage();

        // Act
        apiError.setStatus(500);
        apiError.setError("Internal Server Error");
        apiError.setMessage("An error occurred");
        apiError.setPath("/api/error");

        // Assert
        assertThat(apiError.getStatus()).isEqualTo(500);
        assertThat(apiError.getError()).isEqualTo("Internal Server Error");
        assertThat(apiError.getMessage()).isEqualTo("An error occurred");
        assertThat(apiError.getPath()).isEqualTo("/api/error");
    }
}
