#!/bin/bash

# Script para produzir mensagens de teste no tópico brain-health-request
# Uso: ./produce-test-messages.sh

echo "======================================"
echo "Produtor de Mensagens de Teste"
echo "======================================"
echo ""

# Mensagem 1: Saúde Mental
echo "📤 Enviando mensagem 1: Saúde Mental"
echo '{
  "messageId": "msg-'$(uuidgen)'",
  "userId": "user-001",
  "question": "Como melhorar minha saúde mental e reduzir o estresse?",
  "category": "Saúde Mental",
  "requestedAt": "'$(date -u +"%Y-%m-%dT%H:%M:%S")'",
  "correlationId": "corr-'$(uuidgen)'"
}' | docker exec -i kafka kafka-console-producer \
    --bootstrap-server localhost:9092 \
    --topic brain-health-request

sleep 2

# Mensagem 2: Produtividade
echo "📤 Enviando mensagem 2: Produtividade"
echo '{
  "messageId": "msg-'$(uuidgen)'",
  "userId": "user-002",
  "question": "Quais são as melhores técnicas para aumentar a concentração?",
  "category": "Produtividade",
  "requestedAt": "'$(date -u +"%Y-%m-%dT%H:%M:%S")'",
  "correlationId": "corr-'$(uuidgen)'"
}' | docker exec -i kafka kafka-console-producer \
    --bootstrap-server localhost:9092 \
    --topic brain-health-request

sleep 2

# Mensagem 3: Sono
echo "📤 Enviando mensagem 3: Sono"
echo '{
  "messageId": "msg-'$(uuidgen)'",
  "userId": "user-003",
  "question": "Como posso melhorar a qualidade do meu sono?",
  "category": "Sono e Descanso",
  "requestedAt": "'$(date -u +"%Y-%m-%dT%H:%M:%S")'",
  "correlationId": "corr-'$(uuidgen)'"
}' | docker exec -i kafka kafka-console-producer \
    --bootstrap-server localhost:9092 \
    --topic brain-health-request

sleep 2

# Mensagem 4: Memória
echo "📤 Enviando mensagem 4: Memória"
echo '{
  "messageId": "msg-'$(uuidgen)'",
  "userId": "user-004",
  "question": "Existem exercícios para melhorar a memória?",
  "category": "Memória e Cognição",
  "requestedAt": "'$(date -u +"%Y-%m-%dT%H:%M:%S")'",
  "correlationId": "corr-'$(uuidgen)'"
}' | docker exec -i kafka kafka-console-producer \
    --bootstrap-server localhost:9092 \
    --topic brain-health-request

echo ""
echo "✅ 4 mensagens enviadas com sucesso!"
echo ""
echo "💡 Para consumir as respostas, execute:"
echo "   ./consume-responses.sh"
