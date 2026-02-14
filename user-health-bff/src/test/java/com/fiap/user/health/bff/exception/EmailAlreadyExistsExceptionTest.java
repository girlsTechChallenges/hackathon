package com.fiap.user.health.bff.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailAlreadyExistsException - Testes Unitários")
class EmailAlreadyExistsExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com email duplicado")
    void shouldCreateExceptionWithDuplicateEmail() {
        // Arrange
        String email = "duplicate@example.com";

        // Act
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(email);

        // Assert
        assertThat(exception.getMessage()).isEqualTo("Email already registered: duplicate@example.com");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Deve manter stack trace da exceção")
    void shouldMaintainStackTrace() {
        // Act
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("test@email.com");

        // Assert
        assertThat(exception.getStackTrace()).isNotEmpty();
    }
}
