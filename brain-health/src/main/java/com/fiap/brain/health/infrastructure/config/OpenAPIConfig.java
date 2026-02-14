package com.fiap.brain.health.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI brainHealthOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers());
    }

    private Info apiInfo() {
        return new Info()
                .title("Brain Health API")
                .description("Microserviço de geração de conteúdo médico com IA")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Brain Health Team")
                    .email("brain-health@fiap.com.br"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT"));
    }

    private List<Server> apiServers() {
        return List.of(
            new Server()
                .url("http://localhost:8080")
                .description("Development")
        );
    }
}

