@echo off
REM Script para produzir mensagens de teste no tópico brain-health-request
REM Uso: produce-test-messages.bat

echo ======================================
echo Produtor de Mensagens de Teste
echo ======================================
echo.

REM Gerar timestamp
for /f "tokens=1-6 delims=/-:. " %%a in ("%date% %time%") do (
    set timestamp=%%c-%%a-%%bT%%d:%%e:%%f
)

REM Mensagem 1: Saúde Mental
echo 📤 Enviando mensagem 1: Saúde Mental
(
echo {
echo   "messageId": "msg-001",
echo   "userId": "user-001",
echo   "question": "Como melhorar minha saúde mental e reduzir o estresse?",
echo   "category": "Saúde Mental",
echo   "requestedAt": "2026-02-10T10:30:00",
echo   "correlationId": "corr-001"
echo }
) | docker exec -i kafka kafka-console-producer --bootstrap-server localhost:9092 --topic brain-health-request

timeout /t 2 /nobreak >nul

REM Mensagem 2: Produtividade
echo 📤 Enviando mensagem 2: Produtividade
(
echo {
echo   "messageId": "msg-002",
echo   "userId": "user-002",
echo   "question": "Quais são as melhores técnicas para aumentar a concentração?",
echo   "category": "Produtividade",
echo   "requestedAt": "2026-02-10T10:31:00",
echo   "correlationId": "corr-002"
echo }
) | docker exec -i kafka kafka-console-producer --bootstrap-server localhost:9092 --topic brain-health-request

timeout /t 2 /nobreak >nul

REM Mensagem 3: Sono
echo 📤 Enviando mensagem 3: Sono
(
echo {
echo   "messageId": "msg-003",
echo   "userId": "user-003",
echo   "question": "Como posso melhorar a qualidade do meu sono?",
echo   "category": "Sono e Descanso",
echo   "requestedAt": "2026-02-10T10:32:00",
echo   "correlationId": "corr-003"
echo }
) | docker exec -i kafka kafka-console-producer --bootstrap-server localhost:9092 --topic brain-health-request

timeout /t 2 /nobreak >nul

REM Mensagem 4: Memória
echo 📤 Enviando mensagem 4: Memória
(
echo {
echo   "messageId": "msg-004",
echo   "userId": "user-004",
echo   "question": "Existem exercícios para melhorar a memória?",
echo   "category": "Memória e Cognição",
echo   "requestedAt": "2026-02-10T10:33:00",
echo   "correlationId": "corr-004"
echo }
) | docker exec -i kafka kafka-console-producer --bootstrap-server localhost:9092 --topic brain-health-request

echo.
echo ✅ 4 mensagens enviadas com sucesso!
echo.
echo 💡 Para consumir as respostas, execute:
echo    consume-responses.bat
