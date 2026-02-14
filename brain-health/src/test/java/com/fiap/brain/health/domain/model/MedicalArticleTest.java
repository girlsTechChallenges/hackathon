package com.fiap.brain.health.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MedicalArticle - Testes Unitários")
class MedicalArticleTest {

    private static final String VALID_CONTENT = "Este é um artigo médico sobre os benefícios da caminhada para a saúde cardiovascular.";
    private static final String VALID_URL = "https://cremesp.org.br/article/123";

    @Nested
    @DisplayName("1. Criação de Artigo")
    class ArticleCreationTests {

        @Test
        @DisplayName("Deve criar artigo válido com conteúdo e URL")
        void shouldCreateValidArticle() {
            // Arrange & Act
            MedicalArticle article = new MedicalArticle(VALID_CONTENT, VALID_URL);

            // Assert
            assertNotNull(article, "Artigo não deve ser nulo");
            assertEquals(VALID_CONTENT, article.getContent(), "Conteúdo deve ser igual ao fornecido");
            assertEquals(VALID_URL, article.getArticleUrl(), "URL deve ser igual à fornecida");
            assertNotNull(article.getMetadata(), "Metadata não deve ser nulo");
        }

        @Test
        @DisplayName("Deve criar artigo com metadata customizado")
        void shouldCreateArticleWithCustomMetadata() {
            // Arrange
            MedicalArticle.ArticleMetadata metadata =
                new MedicalArticle.ArticleMetadata("CREMESP", "Dr. João Silva", "2026-02-11");

            // Act
            MedicalArticle article = new MedicalArticle(VALID_CONTENT, VALID_URL, metadata);

            // Assert
            assertNotNull(article.getMetadata(), "Metadata não deve ser nulo");
            assertEquals("CREMESP", article.getMetadata().source());
            assertEquals("Dr. João Silva", article.getMetadata().author());
            assertEquals("2026-02-11", article.getMetadata().publishDate());
        }

        @Test
        @DisplayName("Deve criar metadata vazio quando metadata for null")
        void shouldCreateEmptyMetadataWhenNull() {
            // Arrange & Act
            MedicalArticle article = new MedicalArticle(VALID_CONTENT, VALID_URL, null);

            // Assert
            assertNotNull(article.getMetadata(), "Metadata não deve ser nulo");
            assertEquals("Unknown", article.getMetadata().source());
            assertEquals("Unknown", article.getMetadata().author());
            assertEquals("Unknown", article.getMetadata().publishDate());
        }

        @Test
        @DisplayName("Deve criar artigo usando factory method com sucesso")
        void shouldCreateArticleUsingFactoryMethod() {
            // Arrange & Act
            Optional<MedicalArticle> articleOpt = MedicalArticle.of(VALID_CONTENT, VALID_URL);

            // Assert
            assertTrue(articleOpt.isPresent(), "Optional deve conter artigo");
            assertEquals(VALID_CONTENT, articleOpt.get().getContent());
            assertEquals(VALID_URL, articleOpt.get().getArticleUrl());
        }
    }

    @Nested
    @DisplayName("2. Validações de Conteúdo")
    class ContentValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Deve lançar exceção quando conteúdo for nulo, vazio ou blank")
        void shouldThrowExceptionWhenContentIsNullOrBlank(String invalidContent) {
            // Arrange & Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new MedicalArticle(invalidContent, VALID_URL),
                "Deve lançar IllegalArgumentException"
            );

            assertEquals("Article content cannot be null or blank", exception.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Deve lançar exceção quando URL for nula, vazia ou blank")
        void shouldThrowExceptionWhenUrlIsNullOrBlank(String invalidUrl) {
            // Arrange & Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new MedicalArticle(VALID_CONTENT, invalidUrl),
                "Deve lançar IllegalArgumentException"
            );

            assertEquals("Article URL cannot be null or blank", exception.getMessage());
        }

        @Test
        @DisplayName("Deve retornar Optional.empty quando factory method recebe dados inválidos")
        void shouldReturnEmptyOptionalWhenFactoryMethodReceivesInvalidData() {
            // Arrange & Act
            Optional<MedicalArticle> articleOpt1 = MedicalArticle.of(null, VALID_URL);
            Optional<MedicalArticle> articleOpt2 = MedicalArticle.of(VALID_CONTENT, null);
            Optional<MedicalArticle> articleOpt3 = MedicalArticle.of("", VALID_URL);

            // Assert
            assertTrue(articleOpt1.isEmpty(), "Optional deve estar vazio para conteúdo nulo");
            assertTrue(articleOpt2.isEmpty(), "Optional deve estar vazio para URL nula");
            assertTrue(articleOpt3.isEmpty(), "Optional deve estar vazio para conteúdo vazio");
        }
    }

    @Nested
    @DisplayName("3. Verificação de Conteúdo Mínimo")
    class MinimumContentTests {

        @Test
        @DisplayName("Deve retornar true quando conteúdo atinge tamanho mínimo")
        void shouldReturnTrueWhenContentMeetsMinimumLength() {
            // Arrange
            String content = "A".repeat(100);
            MedicalArticle article = new MedicalArticle(content, VALID_URL);

            // Act
            boolean hasMinimum = article.hasMinimumContent(100);

            // Assert
            assertTrue(hasMinimum, "Deve ter conteúdo mínimo de 100 caracteres");
        }

        @Test
        @DisplayName("Deve retornar true quando conteúdo excede tamanho mínimo")
        void shouldReturnTrueWhenContentExceedsMinimumLength() {
            // Arrange
            String content = "A".repeat(200);
            MedicalArticle article = new MedicalArticle(content, VALID_URL);

            // Act
            boolean hasMinimum = article.hasMinimumContent(100);

            // Assert
            assertTrue(hasMinimum, "Deve ter conteúdo acima do mínimo");
        }

        @Test
        @DisplayName("Deve retornar false quando conteúdo não atinge tamanho mínimo")
        void shouldReturnFalseWhenContentDoesNotMeetMinimumLength() {
            // Arrange
            String content = "A".repeat(50);
            MedicalArticle article = new MedicalArticle(content, VALID_URL);

            // Act
            boolean hasMinimum = article.hasMinimumContent(100);

            // Assert
            assertFalse(hasMinimum, "Não deve ter conteúdo mínimo de 100 caracteres");
        }

        @Test
        @DisplayName("Deve retornar tamanho correto do conteúdo")
        void shouldReturnCorrectContentLength() {
            // Arrange
            String content = "A".repeat(250);
            MedicalArticle article = new MedicalArticle(content, VALID_URL);

            // Act
            int length = article.getContentLength();

            // Assert
            assertEquals(250, length, "Tamanho do conteúdo deve ser 250");
        }
    }

    @Nested
    @DisplayName("4. Verificação de Fonte Confiável")
    class TrustedSourceTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "https://cremesp.org.br/article/123",
            "http://www.cremesp.org.br/pesquisar.php?q=teste",
            "https://pubmed.gov/12345",
            "https://www.pubmed.gov/article",
            "https://scielo.br/article/123",
            "http://www.scielo.br/pdf/test.pdf"
        })
        @DisplayName("Deve identificar URL como fonte confiável")
        void shouldIdentifyTrustedSource(String trustedUrl) {
            // Arrange
            MedicalArticle article = new MedicalArticle(VALID_CONTENT, trustedUrl);

            // Act
            boolean isTrusted = article.isFromTrustedSource();

            // Assert
            assertTrue(isTrusted, "URL deve ser identificada como fonte confiável: " + trustedUrl);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "https://wikipedia.org/article",
            "https://blog.exemplo.com/saude",
            "https://site-desconhecido.com",
            "http://fake-news.com/article"
        })
        @DisplayName("Deve identificar URL como fonte não confiável")
        void shouldIdentifyUntrustedSource(String untrustedUrl) {
            // Arrange
            MedicalArticle article = new MedicalArticle(VALID_CONTENT, untrustedUrl);

            // Act
            boolean isTrusted = article.isFromTrustedSource();

            // Assert
            assertFalse(isTrusted, "URL não deve ser identificada como fonte confiável: " + untrustedUrl);
        }
    }

    @Nested
    @DisplayName("5. Geração de Resumo")
    class SummarizedContentTests {

        @Test
        @DisplayName("Deve retornar conteúdo completo quando menor que tamanho máximo")
        void shouldReturnFullContentWhenSmallerThanMaxLength() {
            // Arrange
            String shortContent = "Conteúdo curto";
            MedicalArticle article = new MedicalArticle(shortContent, VALID_URL);

            // Act
            String summarized = article.getSummarizedContent(100);

            // Assert
            assertEquals(shortContent, summarized, "Deve retornar conteúdo completo");
            assertFalse(summarized.endsWith("..."), "Não deve adicionar '...'");
        }

        @Test
        @DisplayName("Deve retornar conteúdo resumido quando maior que tamanho máximo")
        void shouldReturnSummarizedContentWhenLargerThanMaxLength() {
            // Arrange
            String longContent = "A".repeat(200);
            MedicalArticle article = new MedicalArticle(longContent, VALID_URL);

            // Act
            String summarized = article.getSummarizedContent(50);

            // Assert
            assertEquals(53, summarized.length(), "Deve ter 50 caracteres + '...'");
            assertTrue(summarized.endsWith("..."), "Deve terminar com '...'");
            assertEquals("A".repeat(50) + "...", summarized);
        }

        @Test
        @DisplayName("Deve retornar conteúdo completo quando igual ao tamanho máximo")
        void shouldReturnFullContentWhenEqualToMaxLength() {
            // Arrange
            String content = "A".repeat(100);
            MedicalArticle article = new MedicalArticle(content, VALID_URL);

            // Act
            String summarized = article.getSummarizedContent(100);

            // Assert
            assertEquals(content, summarized, "Deve retornar conteúdo completo");
            assertFalse(summarized.endsWith("..."), "Não deve adicionar '...'");
        }
    }

    @Nested
    @DisplayName("6. Getters e Métodos de Acesso")
    class GetterTests {

        @Test
        @DisplayName("Deve retornar conteúdo correto via getContent()")
        void shouldReturnCorrectContent() {
            // Arrange
            MedicalArticle article = new MedicalArticle(VALID_CONTENT, VALID_URL);

            // Act
            String content = article.getContent();

            // Assert
            assertEquals(VALID_CONTENT, content, "Conteúdo deve ser igual ao fornecido");
        }

        @Test
        @DisplayName("Deve retornar URL correta via getArticleUrl()")
        void shouldReturnCorrectUrl() {
            // Arrange
            MedicalArticle article = new MedicalArticle(VALID_CONTENT, VALID_URL);

            // Act
            String url = article.getArticleUrl();

            // Assert
            assertEquals(VALID_URL, url, "URL deve ser igual à fornecida");
        }

        @Test
        @DisplayName("Deve retornar metadata correto via getMetadata()")
        void shouldReturnCorrectMetadata() {
            // Arrange
            MedicalArticle.ArticleMetadata metadata =
                new MedicalArticle.ArticleMetadata("CREMESP", "Dr. Silva", "2026-02-11");
            MedicalArticle article = new MedicalArticle(VALID_CONTENT, VALID_URL, metadata);

            // Act
            MedicalArticle.ArticleMetadata retrievedMetadata = article.getMetadata();

            // Assert
            assertNotNull(retrievedMetadata);
            assertEquals("CREMESP", retrievedMetadata.source());
            assertEquals("Dr. Silva", retrievedMetadata.author());
            assertEquals("2026-02-11", retrievedMetadata.publishDate());
        }
    }

    @Nested
    @DisplayName("7. ArticleMetadata - Testes de Record")
    class ArticleMetadataTests {

        @Test
        @DisplayName("Deve criar metadata vazio com valores 'Unknown'")
        void shouldCreateEmptyMetadata() {
            // Arrange & Act
            MedicalArticle.ArticleMetadata metadata = MedicalArticle.ArticleMetadata.empty();

            // Assert
            assertNotNull(metadata);
            assertEquals("Unknown", metadata.source());
            assertEquals("Unknown", metadata.author());
            assertEquals("Unknown", metadata.publishDate());
        }

        @Test
        @DisplayName("Deve criar metadata com valores customizados")
        void shouldCreateCustomMetadata() {
            // Arrange & Act
            MedicalArticle.ArticleMetadata metadata =
                new MedicalArticle.ArticleMetadata("PubMed", "Dr. Ana Costa", "2026-01-15");

            // Assert
            assertNotNull(metadata);
            assertEquals("PubMed", metadata.source());
            assertEquals("Dr. Ana Costa", metadata.author());
            assertEquals("2026-01-15", metadata.publishDate());
        }
    }
}
