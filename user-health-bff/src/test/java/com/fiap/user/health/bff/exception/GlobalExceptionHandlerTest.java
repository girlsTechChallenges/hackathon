package com.fiap.user.health.bff.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler - Testes Unitários")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/users/123");
    }

    @Test
    @DisplayName("Deve tratar UserNotFoundException corretamente")
    void shouldHandleUserNotFoundException() {
        // Arrange
        UserNotFoundException exception = new UserNotFoundException(123L);

        // Act
        var response = exceptionHandler.handleUserNotFoundException(exception, request);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains("User not found with id: 123");
        assertThat(response.getBody().getPath()).isEqualTo("/api/users/123");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve tratar EmailAlreadyExistsException corretamente")
    void shouldHandleEmailAlreadyExistsException() {
        // Arrange
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("test@email.com");

        // Act
        var response = exceptionHandler.handleEmailAlreadyExistsException(exception, request);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).contains("Email already registered: test@email.com");
        assertThat(response.getBody().getPath()).isEqualTo("/api/users/123");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve tratar BadCredentialsException corretamente")
    void shouldHandleBadCredentialsException() {
        // Arrange
        BadCredentialsException exception = new BadCredentialsException("Invalid email or password");

        // Act
        var response = exceptionHandler.handleBadCredentialsException(exception, request);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid email or password");
        assertThat(response.getBody().getPath()).isEqualTo("/api/users/123");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException com erros de validação")
    void shouldHandleMethodArgumentNotValidException() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        FieldError fieldError1 = new FieldError("userRequestDto", "email", "must not be blank");
        FieldError fieldError2 = new FieldError("userRequestDto", "nome", "must not be null");

        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Act
        var response = exceptionHandler.handleValidationException(exception, request);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getPath()).isEqualTo("/api/users/123");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getErrors()).hasSize(2);
        assertThat(response.getBody().getErrors().get(0).getField()).isEqualTo("email");
        assertThat(response.getBody().getErrors().get(0).getMessage()).isEqualTo("must not be blank");
        assertThat(response.getBody().getErrors().get(1).getField()).isEqualTo("nome");
        assertThat(response.getBody().getErrors().get(1).getMessage()).isEqualTo("must not be null");
    }

    @Test
    @DisplayName("Deve tratar Exception genérica corretamente")
    void shouldHandleGenericException() {
        // Arrange
        Exception exception = new Exception("Unexpected error occurred");

        // Act
        var response = exceptionHandler.handleGeneralException(exception, request);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).contains("An unexpected error occurred");
        assertThat(response.getBody().getMessage()).contains("Unexpected error occurred");
        assertThat(response.getBody().getPath()).isEqualTo("/api/users/123");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve incluir path correto na resposta de erro")
    void shouldIncludeCorrectPathInErrorResponse() {
        // Arrange
        request.setRequestURI("/api/v1/auth/login");
        UserNotFoundException exception = new UserNotFoundException("User not found");

        // Act
        var response = exceptionHandler.handleUserNotFoundException(exception, request);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/auth/login");
    }
}
