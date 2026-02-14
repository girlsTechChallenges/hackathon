#!/bin/bash

# Script para parar toda a plataforma Health Tech
# Linux/Mac

echo "============================================"
echo "  Health Tech Platform - Shutdown Script"
echo "============================================"
echo ""

# Cores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Menu de opções
echo "Como deseja parar a plataforma?"
echo ""
echo "1) Parar containers (mantém dados)"
echo "2) Parar e remover containers (mantém volumes/dados)"
echo "3) Parar e APAGAR TUDO (remove containers e dados)"
echo ""
read -p "Escolha uma opção [1-3]: " option

case $option in
    1)
        echo ""
        echo "Parando containers..."
        docker-compose stop
        echo -e "${GREEN}✓ Containers parados${NC}"
        echo "Para reiniciar: docker-compose start"
        ;;
    2)
        echo ""
        echo "Parando e removendo containers..."
        docker-compose down
        echo -e "${GREEN}✓ Containers removidos (dados preservados)${NC}"
        echo "Para reiniciar: docker-compose up -d"
        ;;
    3)
        echo ""
        echo -e "${RED}⚠ ATENÇÃO: Isso vai apagar TODOS os dados dos bancos!${NC}"
        read -p "Tem certeza? (digite 'SIM' para confirmar): " confirm
        
        if [ "$confirm" = "SIM" ]; then
            echo ""
            echo "Parando e removendo tudo..."
            docker-compose down -v
            echo -e "${GREEN}✓ Tudo removido (incluindo dados)${NC}"
            echo "Para reiniciar do zero: docker-compose up -d --build"
        else
            echo -e "${YELLOW}Operação cancelada${NC}"
        fi
        ;;
    *)
        echo -e "${RED}Opção inválida!${NC}"
        exit 1
        ;;
esac

echo ""
echo "Para verificar status: docker-compose ps"
echo ""
