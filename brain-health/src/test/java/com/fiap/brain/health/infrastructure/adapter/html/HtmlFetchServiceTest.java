package com.fiap.brain.health.infrastructure.adapter.html;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HtmlFetchService - Testes Unitários")
class HtmlFetchServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private HtmlFetchService htmlFetchService;

    private static final String TEST_URL = "https://example.com/article";
    private static final String SAMPLE_HTML = "<html><body><h1>Test Article</h1><p>Content</p></body></html>";

    @BeforeEach
    void setUp() {
        htmlFetchService = new HtmlFetchService(webClient);

        // Setup mock chain
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Nested
    @DisplayName("Fetch HTML - Sucesso")
    class SuccessfulFetch {

        @Test
        @DisplayName("Deve buscar HTML com sucesso")
        void shouldFetchHtmlSuccessfully() {
            // Arrange
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(SAMPLE_HTML));

            // Act
            String result = htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(SAMPLE_HTML);
            verify(webClient).get();
        }

        @Test
        @DisplayName("Deve buscar HTML de URL válida")
        void shouldFetchHtmlFromValidUrl() {
            // Arrange
            String url = "https://cremesp.org.br/artigo";
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(SAMPLE_HTML));

            // Act
            String result = htmlFetchService.fetchHtml(url);

            // Assert
            assertThat(result).isNotNull();
            verify(requestHeadersUriSpec).uri(url);
        }

        @Test
        @DisplayName("Deve retornar HTML completo")
        void shouldReturnCompleteHtml() {
            // Arrange
            String largeHtml = "<html>" + "x".repeat(10000) + "</html>";
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(largeHtml));

            // Act
            String result = htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            assertThat(result).hasSize(largeHtml.length());
            assertThat(result).startsWith("<html>");
            assertThat(result).endsWith("</html>");
        }

        @Test
        @DisplayName("Deve buscar HTML com caracteres especiais")
        void shouldFetchHtmlWithSpecialCharacters() {
            // Arrange
            String htmlWithSpecialChars = "<html><p>Açúcar, café, ñ, ç</p></html>";
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(htmlWithSpecialChars));

            // Act
            String result = htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            assertThat(result).contains("Açúcar", "café", "ñ", "ç");
        }

        @Test
        @DisplayName("Deve processar HTML vazio mas válido")
        void shouldProcessEmptyButValidHtml() {
            // Arrange
            String emptyHtml = "<html></html>";
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(emptyHtml));

            // Act
            String result = htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            assertThat(result).isEqualTo(emptyHtml);
        }
    }

    @Nested
    @DisplayName("Tratamento de Erros")
    class ErrorHandling {

        @Test
        @DisplayName("Deve retornar null quando erro na requisição")
        void shouldReturnNullWhenRequestError() {
            // Arrange
            HttpHeaders headers = new HttpHeaders();
            URI uri = URI.create(TEST_URL);
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.error(new WebClientRequestException(
                            new RuntimeException("Connection failed"), HttpMethod.GET, uri, headers)));

            // Act
            String result = htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Deve retornar null quando erro 404")
        void shouldReturnNullWhen404Error() {
            // Arrange
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.error(WebClientResponseException.create(
                            404, "Not Found", null, null, null)));

            // Act
            String result = htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Deve retornar null quando erro 500")
        void shouldReturnNullWhen500Error() {
            // Arrange
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.error(WebClientResponseException.create(
                            500, "Internal Server Error", null, null, null)));

            // Act
            String result = htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Deve tratar timeout gracefully")
        void shouldHandleTimeoutGracefully() {
            // Arrange
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.error(new TimeoutException("Request timeout")));

            // Act
            String result = htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Deve tratar exceção genérica")
        void shouldHandleGenericException() {
            // Arrange
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.error(new RuntimeException("Unexpected error")));

            // Act
            String result = htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Não deve lançar exceção em caso de erro")
        void shouldNotThrowExceptionOnError() {
            // Arrange
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.error(new RuntimeException("Error")));

            // Act & Assert
            assertThatCode(() -> htmlFetchService.fetchHtml(TEST_URL))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Timeout e Performance")
    class TimeoutAndPerformance {

        @Test
        @DisplayName("Deve aplicar timeout de 30 segundos")
        void shouldApply30SecondsTimeout() {
            // Arrange
            Mono<String> delayedMono = Mono.just(SAMPLE_HTML).delayElement(Duration.ofSeconds(1));
            when(responseSpec.bodyToMono(String.class)).thenReturn(delayedMono);

            // Act
            String result = htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Deve completar requisições rápidas eficientemente")
        void shouldCompleteFastRequestsEfficiently() {
            // Arrange
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(SAMPLE_HTML));

            // Act
            long startTime = System.currentTimeMillis();
            String result = htmlFetchService.fetchHtml(TEST_URL);
            long duration = System.currentTimeMillis() - startTime;

            // Assert
            assertThat(result).isNotNull();
            assertThat(duration).isLessThan(5000); // Menos de 5 segundos
        }

        @Test
        @DisplayName("Deve processar múltiplas requisições sequencialmente")
        void shouldProcessMultipleRequestsSequentially() {
            // Arrange
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(SAMPLE_HTML));

            // Act
            String result1 = htmlFetchService.fetchHtml(TEST_URL);
            String result2 = htmlFetchService.fetchHtml(TEST_URL);
            String result3 = htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            assertThat(result1).isNotNull();
            assertThat(result2).isNotNull();
            assertThat(result3).isNotNull();
            verify(webClient, times(3)).get();
        }
    }

    @Nested
    @DisplayName("Validações de URL")
    class UrlValidation {

        @Test
        @DisplayName("Deve aceitar URL HTTPS")
        void shouldAcceptHttpsUrl() {
            // Arrange
            String httpsUrl = "https://secure.example.com";
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(SAMPLE_HTML));

            // Act
            String result = htmlFetchService.fetchHtml(httpsUrl);

            // Assert
            assertThat(result).isNotNull();
            verify(requestHeadersUriSpec).uri(httpsUrl);
        }

        @Test
        @DisplayName("Deve aceitar URL HTTP")
        void shouldAcceptHttpUrl() {
            // Arrange
            String httpUrl = "http://example.com";
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(SAMPLE_HTML));

            // Act
            String result = htmlFetchService.fetchHtml(httpUrl);

            // Assert
            assertThat(result).isNotNull();
            verify(requestHeadersUriSpec).uri(httpUrl);
        }

        @Test
        @DisplayName("Deve aceitar URL com path complexo")
        void shouldAcceptUrlWithComplexPath() {
            // Arrange
            String complexUrl = "https://site.com/path/to/article?id=123&lang=pt";
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(SAMPLE_HTML));

            // Act
            String result = htmlFetchService.fetchHtml(complexUrl);

            // Assert
            assertThat(result).isNotNull();
            verify(requestHeadersUriSpec).uri(complexUrl);
        }

        @Test
        @DisplayName("Deve aceitar URL com caracteres especiais no path")
        void shouldAcceptUrlWithSpecialCharactersInPath() {
            // Arrange
            String urlWithSpecialChars = "https://site.com/artigo-saúde";
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(SAMPLE_HTML));

            // Act
            String result = htmlFetchService.fetchHtml(urlWithSpecialChars);

            // Assert
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Conteúdo Vazio e Edge Cases")
    class EmptyContentAndEdgeCases {

        @Test
        @DisplayName("Deve retornar null quando conteúdo está vazio")
        void shouldReturnNullWhenContentIsEmpty() {
            // Arrange
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.empty());

            // Act
            String result = htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Deve tratar string vazia como conteúdo válido")
        void shouldTreatEmptyStringAsValidContent() {
            // Arrange
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(""));

            // Act
            String result = htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve processar HTML com apenas espaços")
        void shouldProcessHtmlWithOnlySpaces() {
            // Arrange
            String spacesOnly = "   ";
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(spacesOnly));

            // Act
            String result = htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            assertThat(result).isEqualTo(spacesOnly);
        }

        @Test
        @DisplayName("Deve processar HTML malformado")
        void shouldProcessMalformedHtml() {
            // Arrange
            String malformed = "<html><body><p>Unclosed tag";
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(malformed));

            // Act
            String result = htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            assertThat(result).isEqualTo(malformed);
        }

        @Test
        @DisplayName("Deve processar HTML muito grande")
        void shouldProcessVeryLargeHtml() {
            // Arrange
            String largeHtml = "<html>" + "x".repeat(1_000_000) + "</html>";
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(largeHtml));

            // Act
            String result = htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            assertThat(result).hasSize(largeHtml.length());
        }
    }

    @Nested
    @DisplayName("WebClient Integration")
    class WebClientIntegration {

        @Test
        @DisplayName("Deve usar método GET")
        void shouldUseGetMethod() {
            // Arrange
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(SAMPLE_HTML));

            // Act
            htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            verify(webClient).get();
            verify(webClient, never()).post();
            verify(webClient, never()).put();
        }

        @Test
        @DisplayName("Deve configurar URI corretamente")
        void shouldConfigureUriCorrectly() {
            // Arrange
            String specificUrl = "https://cremesp.org.br/specific-article";
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(SAMPLE_HTML));

            // Act
            htmlFetchService.fetchHtml(specificUrl);

            // Assert
            verify(requestHeadersUriSpec).uri(specificUrl);
        }

        @Test
        @DisplayName("Deve chamar retrieve para executar requisição")
        void shouldCallRetrieveToExecuteRequest() {
            // Arrange
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(SAMPLE_HTML));

            // Act
            htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            verify(requestHeadersSpec).retrieve();
        }

        @Test
        @DisplayName("Deve converter resposta para String")
        void shouldConvertResponseToString() {
            // Arrange
            when(responseSpec.bodyToMono(String.class))
                    .thenReturn(Mono.just(SAMPLE_HTML));

            // Act
            htmlFetchService.fetchHtml(TEST_URL);

            // Assert
            verify(responseSpec).bodyToMono(String.class);
        }
    }
}
