@echo off
REM Script para subir toda a plataforma Health Tech
REM Windows

echo ============================================
echo   Health Tech Platform - Startup Script
echo ============================================
echo.

REM Verificar se o Docker está rodando
docker info >nul 2>&1
if errorlevel 1 (
    echo [91m❌ Docker não está rodando![0m
    echo Por favor, inicie o Docker Desktop e tente novamente.
    exit /b 1
)

echo [92m✓ Docker está rodando[0m
echo.

REM Verificar se o arquivo de API Key existe
if not exist "brain-health\open_ai_api.env" (
    echo [93m⚠ Arquivo open_ai_api.env não encontrado![0m
    echo.
    set /p api_key="Digite sua OpenAI API Key: "
    
    if "!api_key!"=="" (
        echo [91m❌ API Key não pode estar vazia![0m
        exit /b 1
    )
    
    echo OPENAI_API_KEY=!api_key! > brain-health\open_ai_api.env
    echo [92m✓ Arquivo open_ai_api.env criado[0m
) else (
    echo [92m✓ Arquivo open_ai_api.env encontrado[0m
)

echo.
echo ============================================
echo   Iniciando containers...
echo ============================================
echo.

REM Subir os containers
docker-compose up -d --build

REM Verificar se subiu com sucesso
if errorlevel 1 (
    echo.
    echo [91m❌ Erro ao iniciar a plataforma![0m
    echo Verifique os logs com: docker-compose logs
    exit /b 1
)

echo.
echo ============================================
echo [92m✓ Plataforma iniciada com sucesso![0m
echo ============================================
echo.
echo 📦 Containers rodando:
docker-compose ps
echo.
echo 🌐 URLs de Acesso:
echo.
echo User Health BFF:
echo   - API: http://localhost:8080
echo   - Swagger: http://localhost:8080/swagger-ui.html
echo.
echo Check Health:
echo   - API: http://localhost:8081
echo   - Swagger: http://localhost:8081/swagger-ui/index.html
echo.
echo Brain Health:
echo   - API: http://localhost:9090
echo   - Swagger: http://localhost:9090/swagger-ui.html
echo.
echo Kafka UIs:
echo   - Kafka UI: http://localhost:8085
echo   - Kafdrop: http://localhost:9000
echo.
echo 📖 Guia completo: DOCKER-COMPOSE-GUIDE.md
echo.
echo Para ver os logs:
echo   docker-compose logs -f
echo.
echo Para parar:
echo   docker-compose down
echo.
