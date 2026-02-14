package com.fiap.user.health.bff.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserNotFoundException - Testes Unitários")
class UserNotFoundExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com ID do usuário")
    void shouldCreateExceptionWithUserId() {
        // Arrange
        Long userId = 123L;

        // Act
        UserNotFoundException exception = new UserNotFoundException(userId);

        // Assert
        assertThat(exception.getMessage()).isEqualTo("User not found with id: 123");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Deve criar exceção com mensagem customizada")
    void shouldCreateExceptionWithCustomMessage() {
        // Arrange
        String customMessage = "User not found with email: test@example.com";

        // Act
        UserNotFoundException exception = new UserNotFoundException(customMessage);

        // Assert
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Deve manter stack trace da exceção")
    void shouldMaintainStackTrace() {
        // Act
        UserNotFoundException exception = new UserNotFoundException(1L);

        // Assert
        assertThat(exception.getStackTrace()).isNotEmpty();
    }
}
