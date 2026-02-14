package com.fiap.user.health.bff.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtAccessDeniedHandler - Testes Unitários")
class JwtAccessDeniedHandlerTest {

    private JwtAccessDeniedHandler accessDeniedHandler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        accessDeniedHandler = new JwtAccessDeniedHandler();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("Deve retornar 403 quando acesso for negado")
    void shouldReturn403WhenAccessDenied() throws Exception {
        // Arrange
        request.setRequestURI("/api/admin/users");
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");

        // Act
        accessDeniedHandler.handle(request, response, accessDeniedException);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(response.getContentType()).isEqualTo("application/json");
    }

    @Test
    @DisplayName("Deve retornar mensagem de erro JSON formatada")
    void shouldReturnFormattedJsonErrorMessage() throws Exception {
        // Arrange
        request.setRequestURI("/api/admin/settings");
        AccessDeniedException accessDeniedException = new AccessDeniedException("Insufficient permissions");

        // Act
        accessDeniedHandler.handle(request, response, accessDeniedException);

        // Assert
        String responseContent = response.getContentAsString();
        assertThat(responseContent).contains("\"status\":403");
        assertThat(responseContent).contains("\"error\":\"Forbidden\"");
        assertThat(responseContent).contains("\"message\":\"Access denied. You do not have permission to access this resource..\"");
        assertThat(responseContent).contains("\"path\":\"/api/admin/settings\"");
        assertThat(responseContent).contains("\"timestamp\"");
    }

    @Test
    @DisplayName("Deve incluir timestamp na resposta")
    void shouldIncludeTimestampInResponse() throws Exception {
        // Arrange
        request.setRequestURI("/api/protected");
        AccessDeniedException accessDeniedException = new AccessDeniedException("Test");

        // Act
        accessDeniedHandler.handle(request, response, accessDeniedException);

        // Assert
        String responseContent = response.getContentAsString();
        assertThat(responseContent).contains("timestamp");
    }

    @Test
    @DisplayName("Deve incluir path correto na resposta")
    void shouldIncludeCorrectPathInResponse() throws Exception {
        // Arrange
        request.setRequestURI("/api/v1/admin/dashboard");
        AccessDeniedException accessDeniedException = new AccessDeniedException("Forbidden");

        // Act
        accessDeniedHandler.handle(request, response, accessDeniedException);

        // Assert
        String responseContent = response.getContentAsString();
        assertThat(responseContent).contains("\"/api/v1/admin/dashboard\"");
    }
}
