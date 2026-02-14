#!/bin/bash

# Script para subir toda a plataforma Health Tech
# Linux/Mac

echo "============================================"
echo "  Health Tech Platform - Startup Script"
echo "============================================"
echo ""

# Cores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Verificar se o Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker não está rodando!${NC}"
    echo "Por favor, inicie o Docker Desktop e tente novamente."
    exit 1
fi

echo -e "${GREEN}✓ Docker está rodando${NC}"
echo ""

# Verificar se o arquivo de API Key existe
if [ ! -f "brain-health/open_ai_api.env" ]; then
    echo -e "${YELLOW}⚠ Arquivo open_ai_api.env não encontrado!${NC}"
    echo ""
    read -p "Digite sua OpenAI API Key: " api_key
    
    if [ -z "$api_key" ]; then
        echo -e "${RED}❌ API Key não pode estar vazia!${NC}"
        exit 1
    fi
    
    echo "OPENAI_API_KEY=$api_key" > brain-health/open_ai_api.env
    echo -e "${GREEN}✓ Arquivo open_ai_api.env criado${NC}"
else
    echo -e "${GREEN}✓ Arquivo open_ai_api.env encontrado${NC}"
fi

echo ""
echo "============================================"
echo "  Iniciando containers..."
echo "============================================"
echo ""

# Subir os containers
docker-compose up -d --build

# Verificar se subiu com sucesso
if [ $? -eq 0 ]; then
    echo ""
    echo "============================================"
    echo -e "${GREEN}✓ Plataforma iniciada com sucesso!${NC}"
    echo "============================================"
    echo ""
    echo "📦 Containers rodando:"
    docker-compose ps
    echo ""
    echo "🌐 URLs de Acesso:"
    echo ""
    echo "User Health BFF:"
    echo "  - API: http://localhost:8080"
    echo "  - Swagger: http://localhost:8080/swagger-ui.html"
    echo ""
    echo "Check Health:"
    echo "  - API: http://localhost:8081"
    echo "  - Swagger: http://localhost:8081/swagger-ui/index.html"
    echo ""
    echo "Brain Health:"
    echo "  - API: http://localhost:9090"
    echo "  - Swagger: http://localhost:9090/swagger-ui.html"
    echo ""
    echo "Kafka UIs:"
    echo "  - Kafka UI: http://localhost:8085"
    echo "  - Kafdrop: http://localhost:9000"
    echo ""
    echo "📖 Guia completo: DOCKER-COMPOSE-GUIDE.md"
    echo ""
    echo "Para ver os logs:"
    echo "  docker-compose logs -f"
    echo ""
    echo "Para parar:"
    echo "  docker-compose down"
    echo ""
else
    echo ""
    echo -e "${RED}❌ Erro ao iniciar a plataforma!${NC}"
    echo "Verifique os logs com: docker-compose logs"
    exit 1
fi
