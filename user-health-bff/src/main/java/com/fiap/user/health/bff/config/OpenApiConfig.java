package com.fiap.user.health.bff.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("User Health API")
                        .version("1.0.0")
                        .description("API para gerenciamento de Usuários com autenticação JWT")
                        .contact(new Contact()
                                .name("Equipe Girls Tech Challenges")
                                .email("contato@girlstechchallenges.com")
                        )
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("User Health API - Servidor Local (Desenvolvimento)")
                ))
                // Adiciona o requisito de segurança globalmente
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                                        .description("Informe o token JWT obtido no endpoint /api/v1/auth/login")
                        )
                        .addResponses("200", new ApiResponse().description("Success"))
                        .addResponses("202", new ApiResponse().description("Accepted"))
                        .addResponses("204", new ApiResponse().description("No Content"))
                        .addResponses("400", new ApiResponse().description("Bad Request"))
                        .addResponses("401", new ApiResponse().description("Unauthorized - Token inválido ou ausente"))
                        .addResponses("403", new ApiResponse().description("Forbidden - Acesso negado"))
                        .addResponses("404", new ApiResponse().description("Not Found"))
                        .addResponses("409", new ApiResponse().description("Conflict"))
                        .addResponses("500", new ApiResponse().description("Internal Server Error"))
                );
    }
}