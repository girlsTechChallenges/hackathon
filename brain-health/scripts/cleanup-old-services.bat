@echo off
REM Script para remover arquivos obsoletos após refatoração (Windows)

echo Removendo servicos de busca obsoletos...

REM Remove servicos de multiplas fontes
del /F /Q src\main\java\com\fiap\brain\health\service\ScientificArticleSearchService.java 2>nul
del /F /Q src\main\java\com\fiap\brain\health\service\sources\AbstractArticleSearchService.java 2>nul
del /F /Q src\main\java\com\fiap\brain\health\service\sources\BvsSearchService.java 2>nul
del /F /Q src\main\java\com\fiap\brain\health\service\sources\GoogleScholarSearchService.java 2>nul
del /F /Q src\main\java\com\fiap\brain\health\service\sources\ScieloSearchService.java 2>nul

REM Remove extractors complexos (nao mais necessarios)
del /F /Q src\main\java\com\fiap\brain\health\service\extractor\ArticleContentExtractorService.java 2>nul
del /F /Q src\main\java\com\fiap\brain\health\service\extractor\ArticleUrlExtractorService.java 2>nul

REM Remove servicos HTML complexos (mantem apenas HtmlFetchService)
del /F /Q src\main\java\com\fiap\brain\health\service\html\HtmlRetryService.java 2>nul
del /F /Q src\main\java\com\fiap\brain\health\service\html\HtmlBlockDetectionService.java 2>nul

REM Remove diretorios vazios
rmdir /Q src\main\java\com\fiap\brain\health\service\sources 2>nul
rmdir /Q src\main\java\com\fiap\brain\health\service\extractor 2>nul

echo Limpeza concluida!
echo.
echo Arquivos mantidos:
echo   - CremespSearchService.java (NOVO - servico simplificado)
echo   - IntelligenceArtificialService.java (ATUALIZADO)
echo   - HtmlFetchService.java (mantido para buscar HTML)
echo   - WebClientConfig.java (mantido para SSL)
echo.
pause
