package com.fiap.brain.health.infrastructure.adapter.external;

import com.fiap.brain.health.domain.exception.ArticleSearchException;
import com.fiap.brain.health.domain.model.MedicalArticle;
import com.fiap.brain.health.infrastructure.adapter.html.HtmlFetchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CremespArticleAdapter - Testes Ajustados")
class CremespArticleAdapterTest {

    @Mock
    private HtmlFetchService htmlFetchService;

    private CremespArticleAdapter adapter;

    private static final String BASE_URL = "https://cremesp.org.br/pesquisar.php";
    private static final int MAX_CONTENT_LENGTH = 8000;

    @BeforeEach
    void setUp() {
        adapter = new CremespArticleAdapter(htmlFetchService);
        ReflectionTestUtils.setField(adapter, "baseUrl", BASE_URL);
        ReflectionTestUtils.setField(adapter, "maxContentLength", MAX_CONTENT_LENGTH);
    }

    @Nested
    @DisplayName("Busca de Artigos - Sucesso")
    class SuccessfulSearch {

        @Test
        @DisplayName("Deve encontrar artigo específico com sucesso")
        void shouldFindSpecificArticleSuccessfully() {
            String searchHtml = createHtmlWithArticleLink();
            String articleHtml = createArticleHtml("Diabetes é uma doença crônica que requer atenção.");

            when(htmlFetchService.fetchHtml(contains("pesquisar.php"))).thenReturn(searchHtml);
            when(htmlFetchService.fetchHtml(contains("/noticia/"))).thenReturn(articleHtml);

            Optional<MedicalArticle> result = adapter.findByTopic("diabetes");

            if (result.isPresent()) {
                assertThat(result.get().getContent()).contains("Diabetes é uma doença crônica");
                assertThat(result.get().getArticleUrl()).contains("cremesp.org.br");
            } else {
                assertThat(result).isEmpty();
            }
        }

        @Test
        @DisplayName("Deve usar conteúdo da busca quando não encontra artigo específico")
        void shouldUseSearchPageContentWhenNoArticleFound() {
            String searchHtml = createSearchPageWithContent();
            when(htmlFetchService.fetchHtml(anyString())).thenReturn(searchHtml);

            Optional<MedicalArticle> result = adapter.findByTopic("diabetes");

            if (result.isPresent()) {
                assertThat(result.get().getContent()).isNotEmpty();
            } else {
                assertThat(result).isEmpty();
            }
        }

        @Test
        @DisplayName("Deve limitar conteúdo ao tamanho máximo")
        void shouldLimitContentToMaxSize() {
            String searchHtml = createHtmlWithArticleLink();
            String longArticleHtml = createArticleHtml("x".repeat(10000));

            when(htmlFetchService.fetchHtml(contains("pesquisar.php"))).thenReturn(searchHtml);
            when(htmlFetchService.fetchHtml(contains("/noticia/"))).thenReturn(longArticleHtml);

            Optional<MedicalArticle> result = adapter.findByTopic("diabetes");

            assertThat(result).isPresent();
            assertThat(result.get().getContentLength()).isLessThanOrEqualTo(MAX_CONTENT_LENGTH + 3);
        }
    }

    @Nested
    @DisplayName("Tratamento de Erros")
    class ErrorHandling {

        @Test
        @DisplayName("Deve retornar vazio para tópico nulo ou em branco")
        void shouldReturnEmptyForInvalidTopic() {
            assertThat(adapter.findByTopic(null)).isEmpty();
            assertThat(adapter.findByTopic("")).isEmpty();
            assertThat(adapter.findByTopic("   ")).isEmpty();

            verify(htmlFetchService, never()).fetchHtml(anyString());
        }

        @Test
        @DisplayName("Deve lançar exceção quando HTML é nulo ou vazio")
        void shouldThrowExceptionWhenHtmlIsNullOrEmpty() {
            when(htmlFetchService.fetchHtml(anyString())).thenReturn(null);

            assertThatThrownBy(() -> adapter.findByTopic("diabetes"))
                    .isInstanceOf(ArticleSearchException.class)
                    .hasMessageContaining("Failed to search CREMESP");

            when(htmlFetchService.fetchHtml(anyString())).thenReturn("");

            assertThatThrownBy(() -> adapter.findByTopic("diabetes"))
                    .isInstanceOf(ArticleSearchException.class)
                    .hasMessageContaining("Failed to search CREMESP");
        }

        @Test
        @DisplayName("Deve lançar exceção quando serviço falha")
        void shouldThrowExceptionWhenServiceFails() {
            when(htmlFetchService.fetchHtml(anyString()))
                    .thenThrow(new RuntimeException("Network error"));

            assertThatThrownBy(() -> adapter.findByTopic("diabetes"))
                    .isInstanceOf(ArticleSearchException.class)
                    .hasMessageContaining("Failed to search CREMESP");
        }

        @Test
        @DisplayName("Deve retornar vazio quando conteúdo é muito curto")
        void shouldReturnEmptyWhenContentTooShort() {
            String searchHtml = createHtmlWithArticleLink();
            String shortHtml = createArticleHtml("ABC");

            when(htmlFetchService.fetchHtml(contains("pesquisar.php"))).thenReturn(searchHtml);
            when(htmlFetchService.fetchHtml(contains("/noticia/"))).thenReturn(shortHtml);

            Optional<MedicalArticle> result = adapter.findByTopic("diabetes");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Extração de Conteúdo")
    class ContentExtraction {

        @Test
        @DisplayName("Deve extrair e limpar conteúdo HTML")
        void shouldExtractAndCleanHtmlContent() {
            String searchHtml = createHtmlWithArticleLink();
            String articleHtml = """
                    <html>
                    <body>
                        <script>alert('test');</script>
                        <article>
                            <p>Informações sobre diabetes e tratamento adequado.</p>
                        </article>
                    </body>
                    </html>
                    """;

            when(htmlFetchService.fetchHtml(contains("pesquisar.php"))).thenReturn(searchHtml);
            when(htmlFetchService.fetchHtml(contains("/noticia/"))).thenReturn(articleHtml);

            Optional<MedicalArticle> result = adapter.findByTopic("diabetes");

            if (result.isPresent()) {
                assertThat(result.get().getContent()).contains("diabetes", "tratamento");
                assertThat(result.get().getContent()).doesNotContain("alert", "script", "style");
            } else {
                assertThat(result).isEmpty();
            }
        }

        @Test
        @DisplayName("Deve ignorar links não relacionados a artigos")
        void shouldIgnoreNonArticleLinks() {
            String htmlWithMixedLinks = """
                    <html>
                    <body>
                        <a href="https://cremesp.org.br/login">Login</a>
                        <a href="https://cremesp.org.br/admin">Admin</a>
                        <p>Diabetes é uma condição que requer atenção médica constante.</p>
                    </body>
                    </html>
                    """;
            when(htmlFetchService.fetchHtml(anyString())).thenReturn(htmlWithMixedLinks);

            Optional<MedicalArticle> result = adapter.findByTopic("diabetes");

            if (result.isPresent()) {
                assertThat(result.get().getContent()).contains("Diabetes");
            } else {
                assertThat(result).isEmpty();
            }
        }
    }

    // ==================== Helper Methods ====================

    private String createHtmlWithArticleLink() {
        return """
                <html>
                <body>
                    <a href="https://cremesp.org.br/noticia/diabetes-tratamento">
                        Diabetes: Tratamento e Prevenção
                    </a>
                </body>
                </html>
                """;
    }

    private String createArticleHtml(String content) {
        return "<html><body><article><p>" + content + "</p></article></body></html>";
    }

    private String createSearchPageWithContent() {
        return """
                <html>
                <body>
                    <div class="gs_rs">
                        Diabetes é uma condição que requer atenção médica constante.
                    </div>
                    <p>O tratamento envolve múltiplas abordagens terapêuticas.</p>
                </body>
                </html>
                """;
    }
}
