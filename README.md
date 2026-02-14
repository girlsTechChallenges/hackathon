# 🏥 Health Tech Platform - FIAP Tech Challenge

Plataforma completa de saúde digital composta por 3 microserviços integrados para gerenciamento de usuários, metas de saúde gamificadas e geração de conteúdo médico com IA.

---

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Arquitetura da Plataforma](#-arquitetura-da-plataforma)
- [Microserviços](#-microserviços)
- [Tecnologias](#-tecnologias)
- [Quick Start](#-quick-start)
- [Como Executar Cada Projeto](#-como-executar-cada-projeto)
- [Portas e Acessos](#-portas-e-acessos)
- [Integrações](#-integrações)
- [Troubleshooting](#-troubleshooting)

---

## 🎯 Visão Geral

Esta plataforma oferece uma solução completa para gestão de saúde pessoal, combinando:

1. **User Health BFF** - Gerenciamento de usuários com autenticação JWT
2. **Check Health** - Sistema gamificado de metas de saúde com Kafka
3. **Brain Health** - Geração de conteúdo médico com IA (OpenAI GPT-4)

### Funcionalidades Principais

✅ Autenticação e autorização com JWT (RS256)  
✅ CRUD completo de usuários  
✅ Sistema de metas de saúde gamificadas  
✅ Pontuação e badges por conquistas  
✅ Geração automática de conteúdo médico com IA  
✅ Busca em fontes confiáveis (CREMESP)  
✅ Mensageria assíncrona com Kafka  
✅ Documentação Swagger/OpenAPI completa  
✅ Cobertura de testes 85%+  
✅ Containerização com Docker  

---

## 🏗️ Arquitetura da Plataforma

```
┌─────────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                                │
│                    Web / Mobile Applications                         │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       USER HEALTH BFF                                │
│                    Backend for Frontend                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │
│  │ Autenticação │  │ Gerenciamento│  │  Validações  │             │
│  │     JWT      │  │   Usuários   │  │   & CORS     │             │
│  └──────────────┘  └──────────────┘  └──────────────┘             │
│                         PostgreSQL                                   │
└─────────────────────────────────────────────────────────────────────┘
                             │
                ┌────────────┴────────────┐
                │                         │
                ▼                         ▼
┌──────────────────────────┐    ┌──────────────────────────┐
│    CHECK HEALTH API      │    │    BRAIN HEALTH API      │
│  Sistema de Metas        │    │  Geração de Conteúdo     │
│  ┌───────────────────┐   │    │  ┌───────────────────┐   │
│  │  Gamificação      │   │    │  │  OpenAI GPT-4     │   │
│  │  Pontos & Badges  │   │    │  │  CREMESP Search   │   │
│  └───────────────────┘   │    │  └───────────────────┘   │
│  ┌───────────────────┐   │    │  ┌───────────────────┐   │
│  │  Kafka Events     │   │    │  │  Kafka Events     │   │
│  │  Publisher        │   │    │  │  Consumer         │   │
│  └───────────────────┘   │    │  └───────────────────┘   │
│       PostgreSQL         │    │                          │
└────────────┬─────────────┘    └──────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         KAFKA CLUSTER                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │
│  │  Zookeeper   │  │    Kafka     │  │  Kafka UI    │             │
│  │   Broker     │  │   Messages   │  │   Kafdrop    │             │
│  └──────────────┘  └──────────────┘  └──────────────┘             │
└─────────────────────────────────────────────────────────────────────┘
```

### Princípios Arquiteturais

- **Clean Architecture** - Separação clara de camadas
- **Hexagonal Architecture** - Ports & Adapters
- **Domain-Driven Design** - Foco no domínio de negócio
- **Event-Driven Architecture** - Comunicação assíncrona via Kafka
- **BFF Pattern** - Backend específico para frontend
- **Contract-First** - APIs definidas via OpenAPI
- **SOLID Principles** - Código limpo e manutenível

---

## 📦 Microserviços

### 1. User Health BFF 👤

**Backend for Frontend** para gerenciamento de usuários com autenticação JWT.

#### Características
- **Linguagem:** Java 21
- **Framework:** Spring Boot 4.0.2
- **Banco de Dados:** PostgreSQL 16
- **Autenticação:** JWT com RSA (RS256)
- **Senha:** BCrypt (10 rounds)
- **Documentação:** Swagger/OpenAPI

#### Funcionalidades
- ✅ CRUD completo de usuários
- ✅ Login com geração de JWT token
- ✅ Atualização de senha
- ✅ Validações robustas (Bean Validation)
- ✅ Tratamento de exceções centralizado
- ✅ Health checks e métricas

#### Endpoints Principais
- `POST /api/v1/auth/login` - Login e obtenção de token
- `PATCH /api/v1/auth/password` - Atualizar senha
- `POST /api/users` - Criar usuário (público)
- `GET /api/users` - Listar usuários (protegido)
- `GET /api/users/{id}` - Buscar usuário (protegido)
- `PUT /api/users/{id}` - Atualizar usuário (protegido)
- `DELETE /api/users/{id}` - Deletar usuário (protegido)

📖 **Documentação completa:** [user-health-bff/README.md](user-health-bff/README.md)

---

### 2. Check Health 🎯

**API REST** para criação e acompanhamento de metas de saúde com sistema de gamificação.

#### Características
- **Linguagem:** Java 17
- **Framework:** Spring Boot 4.0.2
- **Banco de Dados:** PostgreSQL 15
- **Mensageria:** Apache Kafka 7.4.0
- **Documentação:** OpenAPI 3.0.3
- **Cobertura de Testes:** 85%+

#### Funcionalidades
- ✅ CRUD completo de metas de saúde
- ✅ Acompanhamento de progresso em tempo real
- ✅ Sistema de gamificação (pontos e badges)
- ✅ Suporte para notificações
- ✅ Metas por período (diária, semanal, mensal, pontual)
- ✅ Níveis de dificuldade (fácil, média, difícil)
- ✅ Publicação de eventos via Kafka

#### Categorias Suportadas
- 💪 **SAUDE_FISICA** - Atividades físicas e exercícios
- 🧠 **SAUDE_MENTAL** - Bem-estar mental e emocional
- 🥗 **NUTRICAO** - Alimentação saudável
- 😴 **SONO** - Qualidade do sono
- 🌟 **BEM_ESTAR** - Bem-estar geral

#### Endpoints Principais
- `POST /goals` - Criar meta
- `GET /goals` - Listar todas as metas
- `GET /goals/{id}` - Buscar meta por ID
- `PUT /goals/{id}` - Atualizar meta
- `DELETE /goals/{id}` - Deletar meta
- `PATCH /goals/{id}/progress` - Atualizar progresso

📖 **Documentação completa:** [check-health/README.md](check-health/README.md)

---

### 3. Brain Health 🧠

**Microserviço** de geração de conteúdo médico com IA, buscando artigos em fontes confiáveis.

#### Características
- **Linguagem:** Java 21
- **Framework:** Spring Boot 3.3.5
- **IA:** OpenAI GPT-4o-mini
- **Mensageria:** Apache Kafka 7.5.0
- **Arquitetura:** Clean + Hexagonal
- **Cobertura de Testes:** 85%+ (179 testes)

#### Funcionalidades
- ✅ Busca artigos médicos em fontes confiáveis (CREMESP)
- ✅ Processamento com IA (OpenAI GPT-4)
- ✅ Geração de conteúdo estruturado com:
  - Introdução
  - Recomendações práticas
  - Conclusão
  - Quiz educativo
- ✅ Consumo de mensagens Kafka
- ✅ Interfaces de teste Kafka (Kafka UI, Kafdrop, REST Proxy)

#### Endpoints Principais
- `POST /api/v1/ai/articles/search` - Buscar e gerar artigo (recomendado)
- `POST /api/ai/article` - Endpoint legado (deprecated)

#### Estrutura da Resposta
```json
{
  "title": "Título do Artigo",
  "introduction": "Introdução sobre o tema...",
  "recommendations": [
    {
      "category": "Categoria",
      "description": "Descrição",
      "tips": ["Dica 1", "Dica 2"]
    }
  ],
  "conclusion": "Conclusão do artigo...",
  "context": "Contexto de Saúde",
  "quizzes": [
    {
      "question": "Pergunta do quiz?",
      "options": ["A", "B", "C", "D"],
      "correctAnswer": "Resposta correta"
    }
  ],
  "sourceLink": "https://fonte.com.br/article/123",
  "timestamp": "2026-02-13 10:30:00"
}
```

📖 **Documentação completa:** [brain-health/README.md](brain-health/README.md)

---

## 🛠 Tecnologias

### Backend
| Tecnologia | user-health-bff | check-health | brain-health |
|------------|----------------|--------------|--------------|
| **Java** | 21 | 17 | 21 |
| **Spring Boot** | 4.0.2 | 4.0.2 | 3.3.5 |
| **Spring Data JPA** | ✅ | ✅ | - |
| **Spring Security** | ✅ JWT | - | - |
| **Spring Kafka** | - | ✅ | ✅ |
| **OpenAI** | - | - | ✅ |

### Banco de Dados
| Database | user-health-bff | check-health | brain-health |
|----------|----------------|--------------|--------------|
| **PostgreSQL** | 16 | 15 | - |
| **H2 (Testes)** | - | ✅ | - |

### Mensageria
| Tecnologia | user-health-bff | check-health | brain-health |
|------------|----------------|--------------|--------------|
| **Apache Kafka** | - | 7.4.0 | 7.5.0 |
| **Zookeeper** | - | 7.4.0 | 7.5.0 |
| **Kafka UI** | - | ✅ | ✅ |
| **Kafdrop** | - | - | ✅ |

### Documentação e Testes
| Tecnologia | user-health-bff | check-health | brain-health |
|------------|----------------|--------------|--------------|
| **Swagger/OpenAPI** | ✅ | ✅ | ✅ |
| **JUnit 5** | ✅ | ✅ | ✅ |
| **Mockito** | ✅ | ✅ | ✅ |
| **Testcontainers** | - | ✅ | - |
| **JaCoCo** | - | ✅ | ✅ |

### DevOps
- **Docker** - Containerização de todos os serviços
- **Docker Compose** - Orquestração de containers
- **Maven** - Gerenciamento de dependências e build

---

## 🚀 Quick Start

### Pré-requisitos

- **Docker** e **Docker Compose** instalados
- **Java 21** (para desenvolvimento local)
- **Maven 3.8+** (para desenvolvimento local)
- **OpenAI API Key** (para Brain Health)

### 1. Clonar o Repositório

```bash
git clone <repository-url>
cd FIAP
```

### 2. Subir Todos os Serviços

#### 🎯 Opção Recomendada: Docker Compose Unificado (NOVO!)

**Sube os 3 projetos de uma vez com um único comando!**

##### Usando Scripts Automatizados (Mais Fácil)

**Windows:**
```powershell
.\start-platform.bat
```

**Linux/Mac:**
```bash
chmod +x start-platform.sh
./start-platform.sh
```

O script automaticamente:
- ✅ Verifica se o Docker está rodando
- ✅ Solicita sua OpenAI API Key (se necessário)
- ✅ Cria o arquivo de configuração
- ✅ Sobe todos os containers
- ✅ Mostra as URLs de acesso

##### Manualmente

```bash
# 1. Configurar OpenAI API Key
# Windows (PowerShell)
Set-Content -Path "brain-health\open_ai_api.env" -Value "OPENAI_API_KEY=sk-your-key-here"

# Linux/Mac
echo "OPENAI_API_KEY=sk-your-key-here" > brain-health/open_ai_api.env

# 2. Subir toda a plataforma (11 containers)
docker-compose up -d --build
```

**O que será iniciado:**
- ✅ User Health BFF (porta 8080)
- ✅ Check Health (porta 8081)
- ✅ Brain Health (porta 9090)
- ✅ 2 bancos PostgreSQL (portas 5432 e 5433)
- ✅ Cluster Kafka completo (porta 9092)
- ✅ Kafka UI, Kafdrop e REST Proxy

**Para parar:**
```bash
# Windows
.\stop-platform.bat

# Linux/Mac
./stop-platform.sh
```

📖 **Guia completo:** [DOCKER-COMPOSE-GUIDE.md](DOCKER-COMPOSE-GUIDE.md)

---

#### Opção A: Executar todos os projetos separadamente

```bash
# User Health BFF
cd user-health-bff
docker-compose up -d
cd ..

# Check Health
cd check-health
docker-compose up -d
cd ..

# Brain Health (configurar OpenAI API Key primeiro)
cd brain-health
# Criar arquivo open_ai_api.env com:
# OPENAI_API_KEY=sk-your-key-here
docker-compose up -d
cd ..
```

#### Opção B: Subir apenas o banco de dados e executar apps localmente

```bash
# User Health BFF - banco de dados
cd user-health-bff
docker-compose -f docker-compose-local.yml up -d
mvn spring-boot:run
cd ..

# Check Health - infraestrutura completa
cd check-health
docker-compose up -d
mvn spring-boot:run
cd ..

# Brain Health - infraestrutura completa
cd brain-health
docker-compose up kafka kafka-ui zookeeper -d
mvn spring-boot:run
cd ..
```

### 3. Verificar Status

```bash
# Verificar containers rodando
docker ps

# Verificar health de cada serviço
curl http://localhost:8080/actuator/health  # user-health-bff
curl http://localhost:8080/actuator/health  # check-health
curl http://localhost:9090/actuator/health  # brain-health
```

---

## 📝 Como Executar Cada Projeto

### User Health BFF

#### Modo 1: Docker (Completo)
```bash
cd user-health-bff
docker-compose up -d --build
```

#### Modo 2: Local (Apenas DB no Docker)
```bash
cd user-health-bff
docker-compose -f docker-compose-local.yml up -d
mvn spring-boot:run
```

**Acessar:**
- Swagger: http://localhost:8080/swagger-ui.html
- API: http://localhost:8080/api/users
- Health: http://localhost:8080/actuator/health

---

### Check Health

#### Modo 1: Docker (Completo)
```bash
cd check-health
docker-compose up -d --build
```

#### Modo 2: Local (Infraestrutura no Docker)
```bash
cd check-health
docker-compose up postgres kafka zookeeper kafka-ui -d
mvn spring-boot:run
```

**Acessar:**
- Swagger: http://localhost:8080/swagger-ui/index.html
- API: http://localhost:8080/goals
- Kafka UI: http://localhost:8090
- Health: http://localhost:8080/actuator/health

---

### Brain Health

#### Pré-requisito: Configurar OpenAI API Key

```bash
cd brain-health

# Windows (PowerShell)
Set-Content -Path "open_ai_api.env" -Value "OPENAI_API_KEY=sk-your-key-here"

# Linux/Mac
echo "OPENAI_API_KEY=sk-your-key-here" > open_ai_api.env
```

#### Modo 1: Docker (Completo)
```bash
cd brain-health
docker-compose up -d --build
```

#### Modo 2: Local (Kafka no Docker)
```bash
cd brain-health
docker-compose up kafka zookeeper kafka-ui kafdrop -d

# Configurar variável de ambiente
# Windows (PowerShell)
$env:OPENAI_API_KEY="sk-your-key-here"

# Linux/Mac
export OPENAI_API_KEY="sk-your-key-here"

mvn spring-boot:run
```

**Acessar:**
- Swagger: http://localhost:9090/swagger-ui.html
- API: http://localhost:9090/api/v1/ai/articles/search
- Kafka UI: http://localhost:8085
- Kafdrop: http://localhost:9000
- Health: http://localhost:9090/actuator/health

---

## 🔌 Portas e Acessos

### User Health BFF
| Serviço | Porta | URL |
|---------|-------|-----|
| API | 8080 | http://localhost:8080 |
| Swagger UI | 8080 | http://localhost:8080/swagger-ui.html |
| PostgreSQL | 5432 | localhost:5432 |
| Actuator | 8080 | http://localhost:8080/actuator |

**Credenciais PostgreSQL:**
- Database: `postgres`
- User: `postgres`
- Password: `postgres`

---

### Check Health
| Serviço | Porta | URL |
|---------|-------|-----|
| API (standalone) | 8080 | http://localhost:8080 |
| API (docker-compose unificado) | 8081 | http://localhost:8081 |
| Swagger UI (standalone) | 8080 | http://localhost:8080/swagger-ui/index.html |
| Swagger UI (unificado) | 8081 | http://localhost:8081/swagger-ui/index.html |
| PostgreSQL (standalone) | 5432 | localhost:5432 |
| PostgreSQL (unificado) | 5433 | localhost:5433 |
| Kafka | 9092 | localhost:9092 |
| Zookeeper | 2181 | localhost:2181 |
| Kafka UI | 8090 | http://localhost:8090 |
| Actuator (standalone) | 8080 | http://localhost:8080/actuator |
| Actuator (unificado) | 8081 | http://localhost:8081/actuator |

**Credenciais PostgreSQL:**
- Database: `checkhealth`
- User: `admin`
- Password: `admin123`

**⚠️ Conflito de Portas Resolvido:** 
- No docker-compose unificado, Check Health usa porta **8081** (API) e **5433** (DB)
- User Health BFF mantém porta **8080** (API) e **5432** (DB)

---

### Brain Health
| Serviço | Porta | URL |
|---------|-----|-----|
| API | 9090 | http://localhost:9090 |
| Swagger UI | 9090 | http://localhost:9090/swagger-ui.html |
| Kafka | 9092 | localhost:9092 |
| Zookeeper | 2181 | localhost:2181 |
| Kafka UI | 8085 | http://localhost:8085 |
| Kafdrop | 9000 | http://localhost:9000 |
| Kafka REST Proxy | 8082 | http://localhost:8082 |
| Actuator | 9090 | http://localhost:9090/actuator |

**⚠️ Kafka Compartilhado:** No docker-compose unificado, Brain Health e Check Health compartilham o mesmo cluster Kafka (uma única instância).

---

## 🔗 Integrações

### User Health BFF ↔️ Check Health

**Fluxo de Integração:**
1. Usuário faz login no **User Health BFF** → Recebe JWT token
2. Com o token, cria metas no **Check Health**
3. **Check Health** valida o user_id (integridade referencial)

**Exemplo de Integração:**
```bash
# 1. Login no User Health BFF
TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"senha123"}' \
  | jq -r '.accessToken')

# 2. Criar meta no Check Health usando user_id
curl -X POST http://localhost:8080/goals \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "12345",
    "title": "Caminhar 30 minutos",
    "category": "SAUDE_FISICA",
    "type": "daily",
    "difficulty": "easy"
  }'
```

---

### Check Health ↔️ Brain Health (via Kafka)

**Fluxo de Integração:**
1. **Check Health** publica eventos no Kafka quando:
   - Meta é criada (`GoalCreatedEvent`)
   - Progresso é atualizado (`GoalProgressUpdatedEvent`)
   
2. **Brain Health** consome eventos do Kafka:
   - Processa mensagens de saúde
   - Gera conteúdo personalizado com IA
   - Busca artigos relacionados às categorias das metas

**Tópicos Kafka:**
- `goal-created` - Eventos de criação de metas
- `goal-progress-updated` - Eventos de atualização de progresso
- `health-content-requests` - Requisições de conteúdo de saúde

**Exemplo de Evento:**
```json
{
  "eventType": "GoalCreated",
  "goalId": "123",
  "userId": "12345",
  "category": "SAUDE_MENTAL",
  "timestamp": "2026-02-13T10:30:00",
  "metadata": {
    "title": "Meditar 15 minutos",
    "difficulty": "easy"
  }
}
```

---

## 🧪 Testes

### User Health BFF
```bash
cd user-health-bff
mvn test
```

**Testes Incluídos:**
- Testes unitários de serviços
- Testes de integração de controllers
- Testes de autenticação JWT
- Testes de validação

---

### Check Health
```bash
cd check-health
mvn test

# Com cobertura JaCoCo
mvn clean test jacoco:report
# Relatório: target/site/jacoco/index.html
```

**Estatísticas:**
- ✅ Testes unitários
- ✅ Testes de integração
- ✅ Testes com Testcontainers
- ✅ Testes de Kafka
- 📊 Cobertura: 85%+

📖 Documentação: [check-health/README-TESTES.md](check-health/README-TESTES.md)

---

### Brain Health
```bash
cd brain-health
mvn test

# Com cobertura JaCoCo
mvn clean test jacoco:report
# Relatório: target/site/jacoco/index.html

# Apenas testes unitários
mvn test -Dtest="*Test,!*IntegrationTest,!*E2ETest"

# Apenas testes de integração
mvn test -Dtest="*IntegrationTest"

# Apenas testes E2E
mvn test -Dtest="*E2ETest"
```

**Estatísticas:**
- ✅ 93 Testes Unitários (Domain + Application)
- ✅ 52 Testes de Integração (Controllers + Full Stack)
- ✅ 34 Testes E2E (Fluxos Completos)
- 📊 **Total: 179 testes**
- 📊 Cobertura: 85%+

📖 Documentação: [brain-health/RELATORIO_FINAL_TESTES.md](brain-health/RELATORIO_FINAL_TESTES.md)

---

## 🎯 Testes Manuais

### Teste Completo do Fluxo

#### 1. User Health BFF - Criar Usuário e Fazer Login

```bash
# Criar usuário
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "email": "joao@test.com",
    "login": "joaosilva",
    "senha": "senha12345678"
  }'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@test.com",
    "password": "senha12345678"
  }'
# Salvar o accessToken retornado
```

#### 2. Check Health - Criar Meta de Saúde

```bash
curl -X POST http://localhost:8080/goals \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "1",
    "title": "Meditar diariamente para reduzir ansiedade",
    "description": "Praticar meditação mindfulness por 15 minutos",
    "category": "SAUDE_MENTAL",
    "type": "daily",
    "start_date": "2026-02-13",
    "end_date": "2026-03-13",
    "frequency": {
      "periodicity": "daily",
      "times_per_period": 1
    },
    "difficulty": "easy",
    "reward": {
      "points": 50,
      "badge": "zen_master"
    },
    "status": "active",
    "notifications": true
  }'
```

#### 3. Brain Health - Gerar Conteúdo sobre o Tema

```bash
curl -X POST http://localhost:9090/api/v1/ai/articles/search \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Quais são os benefícios da meditação para reduzir ansiedade?"
  }'
```

---

## 🐛 Troubleshooting

### Conflito de Portas

Se você encontrar erro de porta já em uso:

```bash
# Verificar portas em uso
# Windows (PowerShell)
netstat -ano | findstr ":8080"
netstat -ano | findstr ":5432"
netstat -ano | findstr ":9092"

# Linux/Mac
lsof -i :8080
lsof -i :5432
lsof -i :9092

# Parar containers conflitantes
docker compose down
```

#### Soluções:
1. **User Health BFF vs Check Health (porta 8080):** Execute um de cada vez ou altere a porta de um deles
2. **Kafka (porta 9092):** Use apenas um Kafka cluster por vez ou reconfigure as portas
3. **PostgreSQL (porta 5432):** Cada projeto tem seu próprio banco, mas compartilham a mesma porta. Execute apenas um de cada vez ou altere as portas.

---

### OpenAI API Key Inválida (Brain Health)

```bash
# Verificar se a variável está configurada
# Windows (PowerShell)
$env:OPENAI_API_KEY

# Linux/Mac
echo $OPENAI_API_KEY

# Se vazio, configurar novamente
# Windows (PowerShell)
$env:OPENAI_API_KEY="sk-your-key-here"

# Linux/Mac
export OPENAI_API_KEY="sk-your-key-here"
```

---

### Containers Não Inicializam

```bash
# Ver logs de um container específico
docker logs user-health-bff
docker logs check-health-app
docker logs brain-health-app

# Reiniciar containers
docker-compose down
docker-compose up -d

# Limpar tudo e recomeçar
docker-compose down -v
docker-compose up -d --build
```

---

### Kafka Não Conecta

```bash
# Verificar se Zookeeper e Kafka estão rodando
docker ps | grep -E "zookeeper|kafka"

# Verificar logs do Kafka
docker logs check-health-kafka
docker logs kafka

# Recriar tópicos (se necessário)
cd check-health
# Windows
.\scripts\create-topics.bat

# Linux/Mac
chmod +x scripts/create-topics.sh
./scripts/create-topics.sh
```

---

### Banco de Dados Não Conecta

```bash
# Verificar se PostgreSQL está rodando
docker ps | grep postgres

# Verificar logs
docker logs user-health-db
docker logs check-health-db

# Testar conexão
# Windows (PowerShell)
docker exec -it user-health-db psql -U postgres -d postgres -c "\dt"
docker exec -it check-health-db psql -U admin -d checkhealth -c "\dt"

# Linux/Mac
docker exec -it user-health-db psql -U postgres -d postgres -c "\dt"
docker exec -it check-health-db psql -U admin -d checkhealth -c "\dt"
```

---

## 📚 Documentações Detalhadas

Cada microserviço possui sua própria documentação completa:

### 📖 User Health BFF
- [README.md](user-health-bff/README.md) - Documentação completa
  - Autenticação JWT detalhada
  - Guia de segurança
  - Exemplos de uso da API
  - Troubleshooting específico

### 📖 Check Health
- [README.md](check-health/README.md) - Documentação principal
- [README-TESTES.md](check-health/README-TESTES.md) - Guia de testes
- [KAFKA.md](check-health/KAFKA.md) - Documentação do Kafka
- Arquitetura em camadas
- Exemplos de payloads
- Cobertura de testes detalhada

### 📖 Brain Health
- [README.md](brain-health/README.md) - Documentação principal
- [RELATORIO_FINAL_TESTES.md](brain-health/RELATORIO_FINAL_TESTES.md) - 179 testes
- [JACOCO_COVERAGE_GUIDE.md](brain-health/JACOCO_COVERAGE_GUIDE.md) - Guia de cobertura
- [ARCHITECTURE_DIAGRAMS.md](brain-health/ARCHITECTURE_DIAGRAMS.md) - Diagramas de arquitetura
- [REFACTORING_SUMMARY.md](brain-health/REFACTORING_SUMMARY.md) - Resumo da refatoração
- Clean Architecture + Hexagonal
- Integração com OpenAI
- Testes unitários, integração e E2E

---

## 🎓 Sobre o Projeto

Este projeto foi desenvolvido como parte do **Tech Challenge da FIAP** (Faculdade de Informática e Administração Paulista), curso de Pós-graduação em Arquitetura e Desenvolvimento Java.

### Objetivos de Aprendizado

✅ **Clean Architecture** - Separação de responsabilidades  
✅ **Hexagonal Architecture** - Ports & Adapters  
✅ **Domain-Driven Design** - Foco no domínio  
✅ **Microserviços** - Arquitetura distribuída  
✅ **Event-Driven Architecture** - Mensageria com Kafka  
✅ **Spring Boot** - Framework moderno Java  
✅ **Spring Security** - Autenticação e autorização  
✅ **Docker** - Containerização  
✅ **Testes** - TDD, cobertura de código  
✅ **OpenAPI** - Contract-First Development  

---

## 📄 Licença

Este projeto é licenciado sob a [MIT License](LICENSE).

---

## 👥 Equipe

Projeto desenvolvido por alunos da FIAP - Tech Challenge 2026.

---

## 📞 Contato e Suporte

Para dúvidas, sugestões ou problemas:
- Abra uma issue no repositório
- Consulte as documentações específicas de cada microserviço
- Revise a seção de [Troubleshooting](#-troubleshooting)

---

**Última atualização:** 13/02/2026
