#!/bin/bash

# Script para consumir mensagens do tópico brain-health-response
# Uso: ./consume-responses.sh

echo "======================================"
echo "Consumidor de Respostas"
echo "======================================"
echo "Aguardando mensagens no tópico brain-health-response..."
echo "Pressione Ctrl+C para parar"
echo ""

docker exec -it kafka kafka-console-consumer \
    --bootstrap-server localhost:9092 \
    --topic brain-health-response \
    --from-beginning \
    --property print.key=true \
    --property print.timestamp=true \
    --property key.separator=" | "
