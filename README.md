# 🏥 Health Platform - Ecossistema de Microserviços

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x%20%7C%204.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.5.0-black.svg)](https://kafka.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Plataforma completa de saúde com três microserviços integrados: gerenciamento de usuários, metas de saúde gamificadas e geração de conteúdo médico com IA.

---

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Arquitetura](#-arquitetura)
- [Microserviços](#-microserviços)
- [Infraestrutura](#-infraestrutura)
- [Início Rápido](#-início-rápido)
- [Executando o Projeto](#-executando-o-projeto)
- [Acessando os Serviços](#-acessando-os-serviços)
- [Portas Utilizadas](#-portas-utilizadas)
- [Documentação](#-documentação)
- [Tecnologias](#-tecnologias)

---

## 🎯 Sobre o Projeto

A **Health Platform** é um ecossistema completo de microserviços para gerenciamento de saúde e bem-estar, desenvolvido como parte do Tech Challenge da FIAP. O sistema integra três microserviços independentes que se comunicam através de mensageria assíncrona (Kafka) e compartilham infraestrutura comum (PostgreSQL, Kafka, Zookeeper).

### 🎪 Principais Funcionalidades

- 👤 **Gerenciamento de Usuários** com autenticação JWT
- 🎯 **Sistema de Metas de Saúde** com gamificação
- 🧠 **Geração de Conteúdo Médico** com IA (OpenAI GPT-4)
- 📊 **Monitoramento e Métricas** via Spring Actuator
- 🔄 **Comunicação Assíncrona** via Apache Kafka
- 🐳 **Containerização Completa** com Docker Compose

---

## 🏗 Arquitetura

### Visão Geral do Ecossistema

```
┌─────────────────────────────────────────────────────────────────────┐
│                        HEALTH PLATFORM                              │
│                     (Docker Network: health-network)                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐  │
│  │  User Health BFF │  │  Check Health    │  │  Brain Health   │  │
│  │  :8080           │  │  :8081           │  │  :9090          │  │
│  │                  │  │                  │  │                 │  │
│  │  • JWT Auth      │  │  • Metas Saúde   │  │  • IA OpenAI    │  │
│  │  • User CRUD     │  │  • Gamificação   │  │  • Artigos      │  │
│  │  • Spring Sec    │  │  • Kafka Events  │  │  • Web Scraping │  │
│  └─────────┬────────┘  └─────────┬────────┘  └────────┬────────┘  │
│            │                     │                     │           │
│            └──────────┬──────────┴──────────┬──────────┘           │
│                       │                     │                      │
│            ┌──────────▼──────────┐ ┌────────▼─────────┐            │
│            │   PostgreSQL        │ │   Apache Kafka   │            │
│            │   :5432             │ │   :9092 (ext)    │            │
│            │                     │ │   :9094 (int)    │            │
│            │   • Shared DB       │ │   • Events       │            │
│            │   • Persistence     │ │   • Async Comm   │            │
│            └─────────────────────┘ └────────┬─────────┘            │
│                                             │                      │
│                                  ┌──────────▼─────────┐            │
│                                  │   Zookeeper        │            │
│                                  │   :2181            │            │
│                                  │                    │            │
│                                  │   • Kafka Manager  │            │
│                                  └────────────────────┘            │
│                                                                     │
│                       ┌──────────────────────┐                     │
│                       │   Kafka UI           │                     │
│                       │   :8090              │                     │
│                       │                      │                     │
│                       │   • Monitoring       │                     │
│                       │   • Administration   │                     │
│                       └──────────────────────┘                     │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 🔄 Fluxo de Comunicação

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Cliente   │   JWT   │ User Health  │         │ PostgreSQL  │
│             ├────────►│     BFF      │◄───────►│             │
└─────────────┘         └──────────────┘         └─────────────┘
                               │
                               │ HTTP/REST
                               │
                        ┌──────▼───────┐          ┌─────────────┐
                        │ Check Health │◄────────►│ PostgreSQL  │
                        │     API      │          └─────────────┘
                        └──────┬───────┘
                               │
                               │ Kafka Events
                               │
                        ┌──────▼───────┐
                        │ Brain Health │
                        │   Service    │
                        └──────────────┘
```

---

## 🚀 Microserviços

### 1️⃣ User Health BFF (Backend for Frontend)

**Porta:** `8080`  
**Descrição:** Backend for Frontend para gerenciamento de usuários

**Funcionalidades:**
- ✅ **Autenticação JWT** com RSA (RS256)
- ✅ **CRUD completo** de usuários
- ✅ **Spring Security** com endpoints públicos e protegidos
- ✅ **Criptografia de senhas** com BCrypt
- ✅ **Validações robustas** com Bean Validation
- ✅ **Health checks** e métricas

**Tecnologias:**
- Java 21
- Spring Boot 4.0.2
- Spring Security + OAuth2 Resource Server
- JWT + RSA
- PostgreSQL
- Docker

**Endpoints Principais:**
- `POST /api/users` - Criar usuário (público)
- `POST /api/v1/auth/login` - Login e obter JWT
- `GET /api/users` - Listar usuários (protegido)
- `PUT /api/users/{id}` - Atualizar usuário (protegido)
- `PATCH /api/v1/auth/password` - Atualizar senha (protegido)

📖 **[Documentação Completa →](user-health-bff/README.md)**

---

### 2️⃣ Check Health API

**Porta:** `8081`  
**Descrição:** API de metas de saúde com sistema de gamificação

**Funcionalidades:**
- ✅ **CRUD de metas** de saúde
- ✅ **Sistema de gamificação** (pontos e badges)
- ✅ **Acompanhamento de progresso** em tempo real
- ✅ **Notificações** de lembrete
- ✅ **Publicação de eventos** via Kafka
- ✅ **Cobertura de testes** 85%+

**Categorias de Metas:**
- 💪 Saúde Física
- 🧠 Saúde Mental
- 🥗 Nutrição
- 😴 Sono
- 🌟 Bem-estar

**Tecnologias:**
- Java 17
- Spring Boot 4.0.2
- Spring Data JPA
- Spring Kafka
- PostgreSQL
- Docker

**Endpoints Principais:**
- `POST /goals` - Criar meta
- `GET /goals` - Listar metas
- `PUT /goals/{id}/progress` - Atualizar progresso
- `DELETE /goals/{id}` - Excluir meta

📖 **[Documentação Completa →](check-health/README.md)**

---

### 3️⃣ Brain Health Service

**Porta:** `9090`  
**Descrição:** Microserviço de geração de conteúdo médico com IA

**Funcionalidades:**
- ✅ **Busca de artigos** em fontes confiáveis (CREMESP)
- ✅ **Processamento com IA** (OpenAI GPT-4)
- ✅ **Geração de conteúdo** estruturado
- ✅ **Quiz educativo** automático
- ✅ **Clean Architecture** + Hexagonal
- ✅ **179 testes** com 85%+ cobertura

**Componentes:**
- Introdução
- Recomendações práticas
- Conclusão
- Quiz educativo

**Tecnologias:**
- Java 21
- Spring Boot 3.3.5
- OpenAI API (GPT-4)
- Apache Kafka
- Clean Architecture
- Docker

**Endpoints Principais:**
- `POST /api/v1/ai/articles/search` - Buscar e processar artigo

📖 **[Documentação Completa →](brain-health/README.md)**

---

## 🛠 Infraestrutura

### PostgreSQL
- **Versão:** 16-alpine
- **Porta:** `5432`
- **Usuário:** `postgres`
- **Senha:** `postgres`
- **Database:** `postgres`
- **Descrição:** Banco de dados compartilhado entre os microserviços

### Apache Kafka
- **Versão:** 7.5.0
- **Porta Externa:** `9092` (para acesso do host)
- **Porta Interna:** `9094` (para comunicação entre containers)
- **Descrição:** Message broker para comunicação assíncrona

### Zookeeper
- **Versão:** 7.5.0
- **Porta:** `2181`
- **Descrição:** Gerenciador de cluster do Kafka

### Kafka UI
- **Porta:** `8090`
- **URL:** http://localhost:8090
- **Descrição:** Interface web para gerenciamento do Kafka

---

## ⚡ Início Rápido

### 📦 Pré-requisitos

- **Docker** 20.10+
- **Docker Compose** 2.0+
- **(Opcional)** Java 21+ e Maven 3.8+ para desenvolvimento local

### 🚀 Execução com Um Único Comando

```bash
# Clone o repositório
git clone <url-do-repositorio>
cd Entrega

# Inicie todos os serviços
docker-compose up -d
```

### ⏳ Aguarde os Serviços Iniciarem

```bash
# Monitore os logs
docker-compose logs -f

# Verifique o status
docker-compose ps
```

**Tempo estimado de inicialização:** 2-3 minutos

---

## 🎮 Executando o Projeto

### Modo 1: Docker Compose (Recomendado)

```bash
# Iniciar todos os serviços
docker-compose up -d

# Ver logs de todos os serviços
docker-compose logs -f

# Ver logs de um serviço específico
docker-compose logs -f user-health-bff

# Parar todos os serviços
docker-compose down

# Parar e remover volumes (limpa o banco de dados)
docker-compose down -v
```

### Modo 2: Serviços Individuais

```bash
# Apenas infraestrutura
docker-compose up -d postgres zookeeper kafka kafka-ui

# Adicionar User Health BFF
docker-compose up -d user-health-bff

# Adicionar Check Health
docker-compose up -d check-health-app

# Adicionar Brain Health
docker-compose up -d brain-health-app
```

### Modo 3: Desenvolvimento Local

```bash
# 1. Subir apenas a infraestrutura
docker-compose up -d postgres zookeeper kafka kafka-ui

# 2. Executar microserviços na IDE
# - Abra cada projeto (brain-health, check-health, user-health-bff) na IDE
# - Execute as classes Main de cada um
```

---

## 🌐 Acessando os Serviços

### 🔗 URLs dos Microserviços

| Serviço | URL | Swagger/Docs | Health Check |
|---------|-----|--------------|--------------|
| **User Health BFF** | http://localhost:8080 | http://localhost:8080/swagger-ui.html | http://localhost:8080/actuator/health |
| **Check Health API** | http://localhost:8081 | http://localhost:8081/swagger-ui.html | http://localhost:8081/actuator/health |
| **Brain Health** | http://localhost:9090 | http://localhost:9090/swagger-ui.html | http://localhost:9090/actuator/health |

### 🎛 Ferramentas de Administração

| Ferramenta | URL | Descrição |
|------------|-----|-----------|
| **Kafka UI** | http://localhost:8090 | Interface de gerenciamento do Kafka |
| **PostgreSQL** | `localhost:5432` | Banco de dados (use DBeaver, pgAdmin, etc.) |

### 🧪 Teste Rápido

#### 1. Verificar Health dos Serviços

```bash
# User Health BFF
curl http://localhost:8080/actuator/health

# Check Health API
curl http://localhost:8081/actuator/health

# Brain Health
curl http://localhost:9090/actuator/health
```

#### 2. Criar um Usuário

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "email": "joao@test.com",
    "login": "joaosilva",
    "senha": "senha12345678"
  }'
```

#### 3. Fazer Login e Obter JWT

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@test.com",
    "password": "senha12345678"
  }'
```

#### 4. Criar uma Meta de Saúde

```bash
curl -X POST http://localhost:8081/goals \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "Beber 2L de água",
    "description": "Manter hidratação diária",
    "category": "NUTRICAO",
    "difficulty": "FACIL",
    "targetValue": 2000,
    "unit": "ml",
    "frequency": "DAILY"
  }'
```

#### 5. Buscar Artigo Médico com IA

```bash
curl -X POST http://localhost:9090/api/v1/ai/articles/search \
  -H "Content-Type: application/json" \
  -d '{
    "searchQuery": "benefícios da atividade física"
  }'
```

---

## 🔌 Portas Utilizadas

| Porta | Serviço | Descrição |
|-------|---------|-----------|
| **5432** | PostgreSQL | Banco de dados relacional |
| **2181** | Zookeeper | Coordenador do Kafka |
| **9092** | Kafka (Externa) | Message broker - acesso do host |
| **9094** | Kafka (Interna) | Message broker - comunicação entre containers |
| **8090** | Kafka UI | Interface de gerenciamento do Kafka |
| **8080** | User Health BFF | Backend for Frontend - Usuários |
| **8081** | Check Health API | API de Metas de Saúde |
| **9090** | Brain Health | Serviço de IA e Artigos Médicos |

---

## 📚 Documentação

### 📖 READMEs dos Microserviços

- **[User Health BFF](user-health-bff/README.md)** - Documentação completa do BFF
- **[Check Health API](check-health/README.md)** - Documentação da API de metas
- **[Brain Health Service](brain-health/README.md)** - Documentação do serviço de IA

### 📊 Documentações Técnicas

#### Brain Health
- **[Arquitetura Clean + Hexagonal](brain-health/ARCHITECTURE_REFINEMENT.md)**
- **[Diagramas de Arquitetura](brain-health/ARCHITECTURE_DIAGRAMS.md)**

#### Check Health
- **[Kafka Setup](check-health/KAFKA.md)**
- **[Guia de Testes](check-health/README-TESTES.md)**

### 🔧 APIs Interativas (Swagger)

Acesse as documentações Swagger de cada serviço:

- User Health BFF: http://localhost:8080/swagger-ui.html
- Check Health API: http://localhost:8081/swagger-ui.html
- Brain Health: http://localhost:9090/swagger-ui.html

---

## 🛠 Tecnologias

### Backend & Frameworks

- **Java** 17, 21
- **Spring Boot** 3.3.5, 4.0.2
- **Spring Security** + OAuth2 Resource Server
- **Spring Data JPA**
- **Spring Kafka**
- **Spring Actuator**

### Banco de Dados & Mensageria

- **PostgreSQL** 16
- **Apache Kafka** 7.5.0
- **Zookeeper** 7.5.0

### Inteligência Artificial

- **OpenAI API** (GPT-4)

### DevOps & Containerização

- **Docker** 20.10+
- **Docker Compose** 2.0+

### Documentação & Validação

- **SpringDoc OpenAPI** 2.6.0, 2.7.0
- **Swagger UI** 3.0
- **Bean Validation**

### Utilitários

- **Lombok** - Redução de boilerplate
- **Jackson** - Serialização JSON
- **Bcrypt** - Criptografia de senhas
- **JWT** - JSON Web Tokens
- **RSA** - Criptografia assimétrica

### Testes

- **JUnit 5** - Framework de testes
- **Mockito** - Mocking
- **AssertJ** - Assertions
- **Testcontainers** - Containers para testes
- **JaCoCo** - Cobertura de código

---

## 🎯 Princípios e Boas Práticas

### Arquitetura

- ✅ **Microserviços** - Arquitetura distribuída
- ✅ **Clean Architecture** - Separação de responsabilidades
- ✅ **Hexagonal Architecture** - Ports & Adapters
- ✅ **Event-Driven** - Comunicação assíncrona
- ✅ **Domain-Driven Design (DDD)** - Modelagem rica

### Desenvolvimento

- ✅ **SOLID** - Princípios de design
- ✅ **RESTful APIs** - APIs padronizadas
- ✅ **OpenAPI** - Documentação padronizada
- ✅ **Bean Validation** - Validações declarativas
- ✅ **Exception Handling** - Tratamento centralizado

### Segurança

- ✅ **JWT Authentication** - Autenticação stateless
- ✅ **RSA Encryption** - Criptografia assimétrica
- ✅ **BCrypt** - Hash de senhas
- ✅ **Spring Security** - Framework de segurança

### Qualidade

- ✅ **Cobertura de Testes** - 85%+
- ✅ **Testes Unitários** - JUnit 5
- ✅ **Testes de Integração** - Testcontainers
- ✅ **Health Checks** - Monitoramento
- ✅ **Métricas** - Spring Actuator

---

## 🐛 Troubleshooting

### Problema: Serviços não iniciam

```bash
# Verificar logs
docker-compose logs

# Recriar containers
docker-compose down
docker-compose up -d --build
```

### Problema: Erro de conexão com banco de dados

```bash
# Verificar se o PostgreSQL está rodando
docker-compose ps postgres

# Reiniciar apenas o PostgreSQL
docker-compose restart postgres
```

### Problema: Kafka não conecta

```bash
# Verificar se Kafka e Zookeeper estão rodando
docker-compose ps kafka zookeeper

# Reiniciar serviços do Kafka
docker-compose restart zookeeper kafka
```

### Problema: Porta já em uso

```bash
# Windows - Verificar porta
netstat -ano | findstr :8080

# Alterar a porta no docker-compose.yml
# Exemplo: "8082:8080" para mapear host:container
```

### Problema: JWT inválido

- Verifique se o token não expirou
- Certifique-se de incluir o prefixo "Bearer " no header
- Formato: `Authorization: Bearer seu_token_aqui`

### Limpar Tudo e Recomeçar

```bash
# Parar e remover tudo (containers, volumes, networks)
docker-compose down -v

# Limpar imagens antigas
docker system prune -a

# Rebuildar e iniciar
docker-compose up -d --build
```

---

## 📝 Variáveis de Ambiente

### User Health BFF

```env
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

### Check Health API

```env
SERVER_PORT=8081
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/postgres
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9094
```

### Brain Health Service

```env
OPENAI_API_KEY=sua_chave_aqui
KAFKA_BOOTSTRAP_SERVERS=kafka:9094
```

**⚠️ Importante:** Para o Brain Health funcionar, você precisa configurar a chave da OpenAI no arquivo `brain-health/open_ai_api.env`

---

## 🚀 Roadmap

### ✅ Implementado

- [x] Autenticação JWT com RSA
- [x] CRUD completo de usuários
- [x] Sistema de metas de saúde
- [x] Gamificação com pontos e badges
- [x] Integração com OpenAI GPT-4
- [x] Comunicação assíncrona via Kafka
- [x] Documentação Swagger completa
- [x] Health checks e métricas
- [x] Docker Compose para todos os serviços
- [x] Testes unitários e de integração

---

## 👥 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

---

## 📧 Contato

**Tech Challenge FIAP** - Turma 2026

---

## 🙏 Agradecimentos

- **FIAP** - Pela oportunidade de aprendizado
- **Spring Framework** - Pelo excelente framework
- **OpenAI** - Pela API de IA
- **Apache Kafka** - Pela plataforma de streaming
- **Docker** - Pela containerização

---
