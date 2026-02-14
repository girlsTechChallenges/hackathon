package com.fiap.brain.health.infrastructure.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Slf4j
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        try {
            SslContext sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            HttpClient httpClient = HttpClient.create()
                    .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

            log.info("WebClient configurado com SSL trust manager permissivo para scraping de sites públicos");

            return WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient));

        } catch (SSLException e) {
            log.error("Erro ao configurar SSL para WebClient: {}", e.getMessage());
            return WebClient.builder();
        }
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
