package com.fiap.user.health.bff.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtAuthenticationEntryPoint - Testes Unitários")
class JwtAuthenticationEntryPointTest {

    private JwtAuthenticationEntryPoint entryPoint;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        entryPoint = new JwtAuthenticationEntryPoint();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("Deve retornar 401 quando autenticação falhar")
    void shouldReturn401WhenAuthenticationFails() throws Exception {
        // Arrange
        request.setRequestURI("/api/users");
        AuthenticationException authException = new AuthenticationException("Authentication failed") {};

        // Act
        entryPoint.commence(request, response, authException);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentType()).isEqualTo("application/json");
    }

    @Test
    @DisplayName("Deve retornar mensagem de erro JSON formatada")
    void shouldReturnFormattedJsonErrorMessage() throws Exception {
        // Arrange
        request.setRequestURI("/api/users/123");
        AuthenticationException authException = new AuthenticationException("Invalid token") {};

        // Act
        entryPoint.commence(request, response, authException);

        // Assert
        String responseContent = response.getContentAsString();
        assertThat(responseContent).contains("\"status\":401");
        assertThat(responseContent).contains("\"error\":\"Unauthorized\"");
        assertThat(responseContent).contains("\"message\":\"Invalid or missing JWT token. Log in to access this resource.\"");
        assertThat(responseContent).contains("\"path\":\"/api/users/123\"");
        assertThat(responseContent).contains("\"timestamp\"");
    }

    @Test
    @DisplayName("Deve incluir timestamp na resposta")
    void shouldIncludeTimestampInResponse() throws Exception {
        // Arrange
        request.setRequestURI("/api/test");
        AuthenticationException authException = new AuthenticationException("Test") {};

        // Act
        entryPoint.commence(request, response, authException);

        // Assert
        String responseContent = response.getContentAsString();
        assertThat(responseContent).contains("timestamp");
    }

    @Test
    @DisplayName("Deve incluir path correto na resposta")
    void shouldIncludeCorrectPathInResponse() throws Exception {
        // Arrange
        request.setRequestURI("/api/v1/auth/protected");
        AuthenticationException authException = new AuthenticationException("No token") {};

        // Act
        entryPoint.commence(request, response, authException);

        // Assert
        String responseContent = response.getContentAsString();
        assertThat(responseContent).contains("\"/api/v1/auth/protected\"");
    }
}
