@echo off
REM Script para criar tópicos Kafka no Windows
REM Uso: create-topics.bat

echo ======================================
echo Criando Tópicos Kafka
echo ======================================

REM Verificar se o Kafka está rodando
docker ps | findstr kafka >nul
if errorlevel 1 (
    echo ❌ Kafka não está rodando. Execute 'docker-compose up -d' primeiro.
    exit /b 1
)

echo ✅ Kafka está rodando
echo.

REM Criar tópico de requisição
echo 📝 Criando tópico: brain-health-request
docker exec kafka kafka-topics --create ^
    --bootstrap-server localhost:9092 ^
    --topic brain-health-request ^
    --partitions 3 ^
    --replication-factor 1 ^
    --if-not-exists

REM Criar tópico de resposta
echo 📝 Criando tópico: brain-health-response
docker exec kafka kafka-topics --create ^
    --bootstrap-server localhost:9092 ^
    --topic brain-health-response ^
    --partitions 3 ^
    --replication-factor 1 ^
    --if-not-exists

echo.
echo ======================================
echo Listando Tópicos Criados
echo ======================================
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

echo.
echo ======================================
echo Detalhes dos Tópicos
echo ======================================
docker exec kafka kafka-topics --describe --bootstrap-server localhost:9092 --topic brain-health-request
docker exec kafka kafka-topics --describe --bootstrap-server localhost:9092 --topic brain-health-response

echo.
echo ✅ Tópicos criados com sucesso!
