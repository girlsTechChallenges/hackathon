@echo off
REM Script para iniciar as interfaces do Kafka para teste

echo ============================================
echo Iniciando Interfaces do Kafka para Teste
echo ============================================
echo.

echo [1/4] Subindo containers do Docker Compose...
docker-compose up -d

echo.
echo [2/4] Aguardando servicos iniciarem (30 segundos)...
timeout /t 30 /nobreak > nul

echo.
echo [3/4] Verificando status dos containers...
docker-compose ps

echo.
echo ============================================
echo Interfaces Disponiveis:
echo ============================================
echo.
echo [1] Kafka UI (Provectus)
echo     URL: http://localhost:8080
echo     Interface moderna e completa
echo.
echo [2] Kafdrop
echo     URL: http://localhost:9000
echo     Interface leve e rapida
echo.
echo [3] Kafka REST Proxy
echo     URL: http://localhost:8082
echo     API REST para testes via HTTP
echo.
echo ============================================
echo.
echo Para parar os servicos, execute:
echo     docker-compose down
echo.
echo Para ver logs em tempo real:
echo     docker-compose logs -f kafka
echo     docker-compose logs -f kafka-ui
echo     docker-compose logs -f kafdrop
echo.
echo Consulte KAFKA_TESTING_GUIDE.md para mais informacoes
echo ============================================

pause
