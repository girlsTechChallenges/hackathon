#!/bin/bash
# Script para remover arquivos obsoletos após refatoração

echo "Removendo serviços de busca obsoletos..."

# Remove serviços de múltiplas fontes
rm -f src/main/java/com/fiap/brain/health/service/ScientificArticleSearchService.java
rm -f src/main/java/com/fiap/brain/health/service/sources/AbstractArticleSearchService.java
rm -f src/main/java/com/fiap/brain/health/service/sources/BvsSearchService.java
rm -f src/main/java/com/fiap/brain/health/service/sources/GoogleScholarSearchService.java
rm -f src/main/java/com/fiap/brain/health/service/sources/ScieloSearchService.java

# Remove extractors complexos (não mais necessários)
rm -f src/main/java/com/fiap/brain/health/service/extractor/ArticleContentExtractorService.java
rm -f src/main/java/com/fiap/brain/health/service/extractor/ArticleUrlExtractorService.java

# Remove serviços HTML complexos (mantém apenas HtmlFetchService)
rm -f src/main/java/com/fiap/brain/health/service/html/HtmlRetryService.java
rm -f src/main/java/com/fiap/brain/health/service/html/HtmlBlockDetectionService.java

# Remove diretórios vazios
rmdir src/main/java/com/fiap/brain/health/service/sources 2>/dev/null
rmdir src/main/java/com/fiap/brain/health/service/extractor 2>/dev/null

echo "Limpeza concluída!"
echo ""
echo "Arquivos mantidos:"
echo "  - CremespSearchService.java (NOVO - serviço simplificado)"
echo "  - IntelligenceArtificialService.java (ATUALIZADO)"
echo "  - HtmlFetchService.java (mantido para buscar HTML)"
echo "  - WebClientConfig.java (mantido para SSL)"
