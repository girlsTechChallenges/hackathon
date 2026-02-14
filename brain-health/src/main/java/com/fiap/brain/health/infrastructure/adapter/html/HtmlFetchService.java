package com.fiap.brain.health.infrastructure.adapter.html;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class HtmlFetchService {

    private final WebClient webClient;

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    public String fetchHtml(String url) {
        try {
            log.debug("Fetching HTML from: {}", url);

            String html = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(TIMEOUT)
                    .onErrorResume(error -> {
                        log.error("Error fetching URL {}: {}", url, error.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (html != null) {
                log.debug("HTML fetched successfully: {} characters", html.length());
            } else {
                log.warn("No HTML content retrieved from: {}", url);
            }

            return html;

        } catch (Exception e) {
            log.error("Failed to fetch HTML from {}: {}", url, e.getMessage(), e);
            return null;
        }
    }
}
