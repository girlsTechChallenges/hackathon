package com.fiap.user.health.bff.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("OpenApiConfig - Testes de Integração")
class OpenApiConfigTest {

    @Autowired
    private OpenAPI openAPI;

    @Test
    @DisplayName("Deve configurar OpenAPI bean corretamente")
    void shouldConfigureOpenApiBean() {
        // Assert
        assertThat(openAPI).isNotNull();
    }

    @Test
    @DisplayName("Deve configurar informações da API")
    void shouldConfigureApiInfo() {
        // Assert
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("User Health API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openAPI.getInfo().getDescription())
                .isEqualTo("API para gerenciamento de Usuários com autenticação JWT");
    }

    @Test
    @DisplayName("Deve configurar informações de contato")
    void shouldConfigureContactInfo() {
        // Assert
        assertThat(openAPI.getInfo().getContact()).isNotNull();
        assertThat(openAPI.getInfo().getContact().getName())
                .isEqualTo("Equipe Girls Tech Challenges");
        assertThat(openAPI.getInfo().getContact().getEmail())
                .isEqualTo("contato@girlstechchallenges.com");
    }

    @Test
    @DisplayName("Deve configurar servidor local")
    void shouldConfigureLocalServer() {
        // Assert
        assertThat(openAPI.getServers()).isNotNull();
        assertThat(openAPI.getServers()).isNotEmpty();
        assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("http://localhost:8080");
        assertThat(openAPI.getServers().get(0).getDescription())
                .contains("User Health API - Servidor Local");
    }

    @Test
    @DisplayName("Deve configurar esquema de segurança JWT")
    void shouldConfigureJwtSecurityScheme() {
        // Assert
        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("bearerAuth");

        var securityScheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertThat(securityScheme).isNotNull();
        assertThat(securityScheme.getType().toString()).isEqualTo("http");
        assertThat(securityScheme.getScheme()).isEqualTo("bearer");
        assertThat(securityScheme.getBearerFormat()).isEqualTo("JWT");
        assertThat(securityScheme.getName()).isEqualTo("Authorization");
    }

    @Test
    @DisplayName("Deve incluir requisito de segurança global")
    void shouldIncludeGlobalSecurityRequirement() {
        // Assert
        assertThat(openAPI.getSecurity()).isNotNull();
        assertThat(openAPI.getSecurity()).isNotEmpty();
        assertThat(openAPI.getSecurity().getFirst().containsKey("bearerAuth")).isTrue();
    }

    @Test
    @DisplayName("Deve configurar respostas padrão")
    void shouldConfigureDefaultResponses() {
        // Assert
        assertThat(openAPI.getComponents().getResponses()).isNotNull();
        assertThat(openAPI.getComponents().getResponses()).containsKey("200");
        assertThat(openAPI.getComponents().getResponses().get("200").getDescription())
                .isEqualTo("Success");
    }
}
