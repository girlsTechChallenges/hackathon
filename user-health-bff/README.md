# 📚 User Health BFF - Documentação Completa

> **Versão:** 1.0 | **Status:** ✅ Produção Ready | **Data:** 09/02/2026

---

## 📑 Índice

1. [Visão Geral](#-visão-geral)
2. [Quick Start](#-quick-start)
3. [Autenticação JWT](#-autenticação-jwt)
4. [Arquitetura](#-arquitetura)
5. [Como Executar](#-como-executar)
6. [API Endpoints](#-api-endpoints)
7. [Testes](#-testes)
8. [Testes Manuais via API](#-testes-manuais-via-api)
9. [Troubleshooting](#-troubleshooting)
10. [Roadmap](#-roadmap)

---

## 🎯 Visão Geral

### Descrição
Backend for Frontend (BFF) para gerenciamento de usuários com CRUD completo, autenticação JWT, validações robustas, tratamento de exceções e documentação Swagger integrada.

### Tecnologias
- **Java 21**
- **Spring Boot 4.0.2**
- **Spring Data JPA**
- **Spring Security** com OAuth2 Resource Server
- **PostgreSQL 42.7.3**
- **JWT com RSA (RS256)**
- **BCrypt** para hash de senhas
- **Lombok**
- **SpringDoc OpenAPI 2.7.0** (Swagger)
- **Bean Validation**
- **Docker & Docker Compose**
- **JUnit 5 + Mockito** para testes

### Funcionalidades
✅ CRUD completo de usuários  
✅ **Autenticação JWT com RSA (RS256)**  
✅ **Criptografia de senhas com BCrypt**  
✅ **Spring Security com endpoints públicos e protegidos**  
✅ **Atualização de senha** (PATCH /api/v1/auth/password)  
✅ Validações de entrada (Bean Validation)  
✅ Tratamento de exceções centralizado  
✅ Documentação Swagger/OpenAPI completa  
✅ Docker Compose para desenvolvimento e produção  
✅ Health checks e métricas (Spring Actuator)  
✅ Persistência com PostgreSQL  
✅ **Testes unitários e de integração**  

---

## 🚀 Quick Start

### 🔧 Modo 1: Desenvolvimento Local (Perfil LOCAL)

```bash
# 1. Subir infraestrutura (PostgreSQL)
docker-compose -f docker-compose-local.yml up -d

# 2. Executar aplicação na IDE
# - Abra UserHealthMain.java no IntelliJ
# - Pressione Shift+F10 (Run)
# - Perfil 'local' será ativado automaticamente
```

### 🐳 Modo 2: Produção no Docker (Perfil PROD)

```bash
# Subir tudo com um comando
docker-compose up -d
```

### Acessar Aplicação
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API REST:** http://localhost:8080/api/users
- **Health Check:** http://localhost:8080/actuator/health
- **Metrics:** http://localhost:8080/actuator/metrics

### Teste Rápido
```bash
# Health check
curl http://localhost:8080/actuator/health

# Criar usuário (público - não requer autenticação)
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"nome":"João Silva","email":"joao@test.com","login":"joaosilva","senha":"senha12345678"}'

# Login e obter JWT token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"joao@test.com","password":"senha12345678"}'

# Listar usuários (protegido - requer token)
curl -H "Authorization: Bearer SEU_TOKEN_AQUI" http://localhost:8080/api/users
```

---

## 🔐 Autenticação JWT

### Visão Geral

A aplicação possui autenticação completa com **JWT (JSON Web Tokens)** usando **Spring Security** e assinatura **RSA (RS256)**.

### Componentes de Segurança
- **Spring Security** - Framework de autenticação e autorização
- **OAuth2 Resource Server** - Validação de tokens JWT
- **JWT com RSA** - Tokens assinados com chaves RSA (RS256)
- **BCrypt** - Hash de senhas com salt automático (10 rounds)
- **Session Stateless** - Sem estado de sessão (escalável)

### Endpoints de Autenticação

#### 1. Login (Público)
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "senha123456"
}
```

**Resposta:** Token JWT válido
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
  "expiresIn": 3600
}
```

#### 2. Atualizar Senha (Público)
```bash
PATCH /api/v1/auth/password
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "novaSenha123456"
}
```

**Resposta:** 204 No Content

#### 3. Usar Token em Requisições Protegidas
```bash
GET /api/users
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
```

### Proteção de Endpoints

| Endpoint | Público | Protegido | Descrição |
|----------|---------|-----------|-----------|
| POST /api/v1/auth/login | ✅ Sim | ❌ Não | Login de usuário |
| PATCH /api/v1/auth/password | ✅ Sim | ❌ Não | Atualizar senha |
| POST /api/users | ✅ Sim | ❌ Não | Criar usuário |
| GET /api/users | ❌ Não | ✅ Sim | Listar usuários |
| GET /api/users/{id} | ❌ Não | ✅ Sim | Buscar usuário |
| PUT /api/users/{id} | ❌ Não | ✅ Sim | Atualizar usuário |
| DELETE /api/users/{id} | ❌ Não | ✅ Sim | Deletar usuário |
| GET /actuator/health | ✅ Sim | ❌ Não | Health check |
| GET /swagger-ui.html | ✅ Sim | ❌ Não | Documentação |

### Segurança Implementada

✅ **Senhas criptografadas** com BCrypt (10 rounds)  
✅ **JWT assinado** com RSA (RS256)  
✅ **Token expira** em 1 hora  
✅ **Session stateless** (escalável)  
✅ **CORS configurado**  
✅ **Exception handling** completo  
✅ **Chaves RSA** em arquivos separados (app.pub e app.key)

### Fluxo de Autenticação

```
1. Criar usuário (POST /api/users)
   → Senha criptografada com BCrypt
   → Usuário salvo no banco

2. Login (POST /api/v1/auth/login)  
   → Valida email/senha
   → Gera JWT token com RSA
   → Retorna token + expiração

3. Requisições protegidas
   → Header: Authorization: Bearer <token>
   → Spring Security valida assinatura RSA
   → Extrai informações do token
   → Permite ou nega acesso
```

### Exemplo Completo

```bash
# 1. Criar usuário
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "email": "joao@example.com",
    "login": "joaosilva",
    "senha": "senha12345678"
  }'

# 2. Fazer login
TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@example.com",
    "password": "senha12345678"
  }' | jq -r '.accessToken')

# 3. Usar token em requisições protegidas
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/users
```

---

## 🏗️ Arquitetura

### Estrutura de Camadas

```
┌─────────────────────────────────────────┐
│         Controller Layer                │  ← REST API
│  - Endpoints HTTP                       │
│  - Validação de entrada (@Valid)        │
│  - Documentação Swagger                 │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│         Service Layer                   │  ← Lógica de Negócio
│  - Regras de negócio                    │
│  - Validações customizadas              │
│  - Transações (@Transactional)          │
│  - Autenticação JWT                     │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│         Repository Layer                │  ← Acesso a Dados
│  - JPA Repository                       │
│  - Queries customizadas                 │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│         Database (PostgreSQL)           │
│  - Tabela: usuarios                     │
└─────────────────────────────────────────┘
```

### Estrutura de Arquivos

```
src/main/java/com/fiap/user/health/bff/
├── UserHealthMain.java              # Main application
├── config/
│   ├── OpenApiConfig.java          # Swagger configuration
│   └── SecurityConfig.java         # Spring Security + JWT
├── controller/
│   ├── UserController.java         # CRUD endpoints
│   ├── AuthController.java         # Authentication endpoints
│   └── docs/                       # Swagger documentation
├── dto/
│   ├── request/
│   │   ├── UserRequestDto.java     # Create user DTO
│   │   ├── UserUpdateRequestDto.java # Update user DTO
│   │   ├── UserCredentialsRequestDto.java # Login DTO
│   │   └── UserAuthRequestDto.java # Token response DTO
│   └── response/
│       └── UserResponseDto.java     # User response DTO
├── exception/
│   ├── ApiErrorMessage.java         # Error response structure
│   ├── EmailAlreadyExistsException.java
│   ├── UserNotFoundException.java
│   ├── JwtAuthenticationEntryPoint.java
│   ├── JwtAccessDeniedHandler.java
│   └── GlobalExceptionHandler.java  # Exception handler
├── mapper/
│   └── UserMapper.java              # DTO/Entity conversions
├── model/
│   ├── User.java                    # Domain model
│   └── Token.java                   # JWT token model
├── persistence/
│   ├── entity/
│   │   └── UserEntity.java         # JPA entity
│   └── repository/
│       └── UserRepository.java      # JPA repository
└── service/
    ├── user/
    │   ├── UserServiceInterface.java
    │   └── UserServiceImpl.java     # User business logic
    └── auth/
        ├── AuthServiceInterface.java
        └── AuthServiceImpl.java     # Authentication logic
```

### Padrões de Projeto Utilizados

| Padrão | Aplicação |
|--------|-----------|
| **Layered Architecture** | Separação Controller/Service/Repository |
| **DTO Pattern** | Separação de objetos de transferência |
| **Repository Pattern** | Abstração de acesso a dados |
| **Mapper Pattern** | Conversões entre camadas |
| **Builder Pattern** | Construção de objetos (Lombok) |
| **Dependency Injection** | Injeção via construtor |
| **Exception Handler Pattern** | Tratamento centralizado |
| **Strategy Pattern** | Services com interfaces |

---

## 🏃 Como Executar

A aplicação oferece **dois modos de execução com Docker**, cada um com seu próprio arquivo docker-compose:

### 🐳 Modo 1: Produção Completa (docker-compose.yml)

**Arquivo:** `docker-compose.yml`

**O que sobe:**
- ✅ PostgreSQL 16 (banco de dados)
- ✅ Aplicação Spring Boot (containerizada)

**Quando usar:** Deploy completo, ambiente de produção, testes de integração com containers.

**Executar:**
```bash
# Subir tudo (build automático da aplicação)
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar
docker-compose down

# Rebuild completo
docker-compose up -d --build
```

**Estrutura do docker-compose.yml:**
```yaml
services:
  app-db:                    # PostgreSQL 16
    image: postgres:16-alpine
    ports: ["5432:5432"]
    
  user-health-bff:           # Aplicação Spring Boot
    build: .                 # Build do Dockerfile
    ports: ["8080:8080"]
    depends_on:
      - app-db
```

**Acessar:**
- Swagger: http://localhost:8080/swagger-ui.html
- API: http://localhost:8080/api/users
- Health: http://localhost:8080/actuator/health

---

### 🔧 Modo 2: Desenvolvimento Local (docker-compose-local.yml)

**Arquivo:** `docker-compose-local.yml`

**O que sobe:**
- ✅ PostgreSQL 16 (banco de dados)
- ❌ Aplicação roda **na IDE** (não no Docker)

**Quando usar:** Desenvolvimento local com hot reload, debugging na IDE.

**Executar:**
```bash
# 1. Subir apenas infraestrutura (PostgreSQL)
docker-compose -f docker-compose-local.yml up -d

# 2. Executar aplicação na IDE
# - IntelliJ: Abra UserHealthMain.java → Run (Shift+F10)
# - Eclipse: Run As → Java Application
# - VSCode: Run Java

# Parar infraestrutura
docker-compose -f docker-compose-local.yml down
```

**Estrutura do docker-compose-local.yml:**
```yaml
services:
  app-db:                    # PostgreSQL 16
    image: postgres:16-alpine
    ports: ["5432:5432"]
```

**Acessar:**
- Aplicação (IDE): http://localhost:8080/swagger-ui.html
- PostgreSQL: localhost:5432

---

### 🎯 Comparação dos Modos

| Aspecto | Produção (`docker-compose.yml`) | Local (`docker-compose-local.yml`) |
|---------|--------------------------------|-----------------------------------|
| **PostgreSQL** | ✅ Container | ✅ Container |
| **Aplicação** | ✅ Container (build) | ❌ Roda na IDE |
| **Hot Reload** | ❌ Requer rebuild | ✅ Automático (IDE) |
| **Debug** | ⚠️ Remoto | ✅ Nativo (IDE) |
| **Uso** | Deploy, Testes E2E | Desenvolvimento |

---

## 📡 API Endpoints

### Visão Geral

| Método | Endpoint | Descrição | Status Sucesso | Status Erro |
|--------|----------|-----------|----------------|-------------|
| POST | `/api/users` | Criar usuário | 201 Created | 400, 409 |
| GET | `/api/users` | Listar todos | 200 OK | - |
| GET | `/api/users/{id}` | Buscar por ID | 200 OK | 404 |
| PUT | `/api/users/{id}` | Atualizar | 200 OK | 400, 404, 409 |
| DELETE | `/api/users/{id}` | Deletar | 204 No Content | 404 |

### Detalhamento

#### 1. Criar Usuário
```http
POST /api/users
Content-Type: application/json

{
  "nome": "João Silva",
  "email": "joao@example.com",
  "login": "joaosilva",
  "senha": "senha12345678"
}
```

**Validações:**
- `nome`: 2-50 caracteres, apenas letras
- `email`: formato válido, único no sistema
- `login`: 5-20 caracteres
- `senha`: 8-100 caracteres

**Respostas:**
- `201 Created`: Usuário criado com sucesso
- `400 Bad Request`: Dados inválidos
- `409 Conflict`: Email já cadastrado

#### 2. Listar Todos
```http
GET /api/users
```

**Resposta:**
```json
[
  {
    "id": 1,
    "nome": "João Silva",
    "email": "joao@example.com",
    "login": "joaosilva"
  }
]
```

#### 3. Buscar por ID
```http
GET /api/users/1
```

**Respostas:**
- `200 OK`: Usuário encontrado
- `404 Not Found`: Usuário não existe

#### 4. Atualizar
```http
PUT /api/users/1
Content-Type: application/json

{
  "email": "novoemail@example.com",
  "login": "novologin",
  "senha": "novasenha123"
}
```

**Respostas:**
- `200 OK`: Atualizado com sucesso
- `400 Bad Request`: Dados inválidos
- `404 Not Found`: Usuário não existe
- `409 Conflict`: Email já usado por outro usuário

#### 5. Deletar
```http
DELETE /api/users/1
```

**Respostas:**
- `204 No Content`: Deletado com sucesso
- `404 Not Found`: Usuário não existe

### Estrutura de Erro

```json
{
  "timestamp": "2026-02-08T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 1",
  "path": "/api/users/1",
  "errors": [
    {
      "field": "email",
      "message": "must be a well-formed email address"
    }
  ]
}
```

---

## 🐳 Docker - Comandos Úteis

### Comandos Básicos

#### Produção (docker-compose.yml)
```bash
# Iniciar tudo
docker-compose up -d

# Iniciar com logs visíveis
docker-compose up

# Parar containers
docker-compose down

# Parar e remover volumes (⚠️ APAGA DADOS!)
docker-compose down -v

# Ver status
docker-compose ps

# Ver logs
docker-compose logs -f

# Logs de serviço específico
docker-compose logs -f user-health-bff

# Rebuild da aplicação
docker-compose up -d --build user-health-bff

# Restart de serviço
docker-compose restart user-health-bff
```

#### Desenvolvimento Local (docker-compose-local.yml)
```bash
# Iniciar infraestrutura
docker-compose -f docker-compose-local.yml up -d

# Parar infraestrutura
docker-compose -f docker-compose-local.yml down

# Logs do PostgreSQL
docker-compose -f docker-compose-local.yml logs -f app-db

# Restart do PostgreSQL
docker-compose -f docker-compose-local.yml restart app-db
```

### Comandos de Debug

```bash
# Entrar no container da aplicação (modo produção)
docker exec -it user-health-bff sh

# Entrar no PostgreSQL
docker exec -it user-health-db psql -U postgres

# Ver uso de recursos
docker stats

# Ver IP dos containers
docker inspect user-health-bff | grep IPAddress

# Verificar health check
docker inspect --format='{{json .State.Health}}' user-health-bff
```

### Limpeza e Manutenção

```bash
# Limpeza leve (containers parados)
docker system prune

# Limpeza completa (⚠️ remove tudo não usado)
docker system prune -a --volumes

# Remover apenas volumes não usados
docker volume prune

# Ver espaço usado
docker system df

# Remover imagem específica
docker rmi user-health-bff-user-health-bff
```

### Troubleshooting Docker

```bash
# Build com logs detalhados
docker-compose build --no-cache --progress=plain

# Verificar network
docker network ls
docker network inspect user-health-network

# Verificar volumes
docker volume ls
docker volume inspect user-health-bff_postgres-data

# Testar conectividade entre containers
docker exec user-health-bff ping app-db
```

---

## 🧪 Testes

### 📊 Cobertura de Testes

A aplicação possui **cobertura excepcional de testes** com **~95% de cobertura geral**.

#### Métricas Gerais

| Métrica | Valor |
|---------|-------|
| **Classes de Teste** | 16 |
| **Total de Testes** | ~130 |
| **Testes com Sucesso** | 100% |
| **Cobertura Geral** | ~95% |
| **Tempo de Execução** | ~28s |

#### Cobertura por Camada

| Camada | Cobertura | Classes Testadas |
|--------|-----------|------------------|
| **Controllers** | ~95% | AuthController, UserController |
| **Services** | ~95% | AuthService, UserService |
| **Repositories** | ~100% | UserRepository |
| **Mappers** | ~100% | UserMapper |
| **Exceptions** | ~98% | GlobalExceptionHandler, Custom Exceptions, JWT Handlers |
| **Config** | ~95% | SecurityConfig, OpenApiConfig |
| **Models/Entities** | ~80% | User, UserEntity, DTOs |

#### Gráfico Visual de Cobertura

```
Exceptions:      ████████████████████░  98%
Config:          ███████████████████░░  95%
Controllers:     ███████████████████░░  95%
Services:        ███████████████████░░  95%
Repositories:    ████████████████████░ 100%
Mappers:         ████████████████████░ 100%
Models/Entities: ████████████████░░░░░  80%
────────────────────────────────────────
COBERTURA GERAL: ███████████████████░░  95%
```

---

### 🗂️ Estrutura de Testes

```
src/test/java/com/fiap/user/health/bff/
├── 🔵 E2E (12 testes)
│   └── UserHealthBffE2ETest.java
│
├── 🟢 Controllers (19 testes)
│   ├── AuthControllerIntegrationTest.java      # 8 testes
│   └── UserControllerIntegrationTest.java      # 11 testes
│
├── 🟡 Services (18 testes)
│   ├── auth/
│   │   └── AuthServiceImplTest.java            # 7 testes
│   └── user/
│       └── UserServiceImplTest.java            # 11 testes
│
├── 🟣 Repository (14 testes)
│   └── UserRepositoryIntegrationTest.java      # 14 testes
│
├── 🔴 Mappers (8 testes)
│   └── UserMapperTest.java                     # 8 testes
│
├── 🟠 Exceptions (27 testes)
│   ├── UserNotFoundExceptionTest.java          # 3 testes
│   ├── EmailAlreadyExistsExceptionTest.java    # 2 testes
│   ├── GlobalExceptionHandlerTest.java         # 7 testes
│   ├── JwtAuthenticationEntryPointTest.java    # 4 testes
│   ├── JwtAccessDeniedHandlerTest.java         # 4 testes
│   └── ApiErrorMessageTest.java                # 7 testes
│
├── ⚪ Config (15 testes)
│   ├── SecurityConfigTest.java                 # 8 testes
│   └── OpenApiConfigTest.java                  # 7 testes
│
└── 🔵 Integration (14 testes)
    └── RealIntegrationTest.java                # 14 testes
```

---

### 🚀 Como Executar os Testes

#### 1️⃣ Executar Todos os Testes

```bash
# Windows
mvnw.cmd clean test

# Linux/Mac
./mvnw clean test
```

**Resultado Esperado:**
```
[INFO] Tests run: ~130, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

#### 2️⃣ Executar com Relatório de Cobertura

```bash
# Gerar relatório JaCoCo
mvnw.cmd clean test jacoco:report

# Visualizar relatório (Windows)
start target\site\jacoco\index.html

# Visualizar relatório (Linux/Mac)
open target/site/jacoco/index.html
```

#### 3️⃣ Executar Testes por Categoria

```bash
# Apenas testes unitários (Services e Mappers)
mvnw.cmd test -Dtest=*ServiceImplTest,*MapperTest

# Apenas testes de integração (Controllers e Repository)
mvnw.cmd test -Dtest=*IntegrationTest,*ControllerIntegrationTest

# Apenas testes E2E
mvnw.cmd test -Dtest=UserHealthBffE2ETest

# Apenas testes de Exceptions
mvnw.cmd test -Dtest=*Exception*Test

# Apenas testes de Config
mvnw.cmd test -Dtest=*Config*Test
```

#### 4️⃣ Executar Teste Específico

```bash
# Teste específico por classe
mvnw.cmd test -Dtest=UserServiceImplTest

# Teste específico por método
mvnw.cmd test -Dtest=UserServiceImplTest#shouldCreateUserSuccessfully
```

---

### 🛠️ Tecnologias de Teste

| Tecnologia | Versão | Uso |
|------------|--------|-----|
| **JUnit 5** | Latest | Framework de testes principal |
| **Mockito** | Latest | Mocks e stubs para testes unitários |
| **Spring Boot Test** | 4.0.2 | Testes de integração |
| **MockMvc** | Spring | Testes de controllers REST |
| **H2 Database** | Latest | Banco em memória para testes |
| **AssertJ** | Latest | Assertions fluentes |
| **JaCoCo** | 0.8.12 | Relatórios de cobertura |

---

### 📋 Tipos de Testes Implementados

#### ✅ Testes Unitários (45 testes)
- **Services** - Lógica de negócio isolada
- **Mappers** - Conversões entre DTOs e Entities
- **Exceptions** - Tratamento de erros customizados

**Características:**
- Muito rápidos (< 50ms cada)
- Isolados com mocks
- Sem dependências externas
- Padrão AAA (Arrange-Act-Assert)

#### ✅ Testes de Integração (47 testes)
- **Controllers** - Endpoints REST com MockMvc
- **Repository** - Queries JPA com H2
- **Integration** - Fluxos completos entre camadas

**Características:**
- Contexto Spring Boot carregado
- Banco H2 em memória
- Validação de integrações reais
- Testes de segurança JWT

#### ✅ Testes E2E (12 testes)
- **UserHealthBffE2ETest** - Fluxos completos de usuário
- CRUD completo (Create → Read → Update → Delete)
- Autenticação e autorização
- Validação de persistência

**Características:**
- Simula cenários reais de uso
- Validação de ponta a ponta
- Contexto completo da aplicação

---

### 🎯 Cenários de Teste Cobertos

#### Funcionalidades Testadas

✅ **CRUD Completo**
- Criação de usuários
- Listagem com paginação
- Busca por ID
- Atualização de dados
- Exclusão de usuários

✅ **Autenticação e Segurança**
- Login com JWT
- Validação de token
- Endpoints protegidos
- Acesso negado (403)
- Não autorizado (401)
- Atualização de senha

✅ **Validações**
- Campos obrigatórios
- Formatos (email, senha)
- Tamanhos mínimos/máximos
- Email duplicado (409)
- Dados inválidos (400)

✅ **Exceções**
- Usuário não encontrado (404)
- Email já existe (409)
- Credenciais inválidas (401)
- Erros de validação (400)
- Erros internos (500)

✅ **Persistência**
- Salvamento no banco
- Queries customizadas
- Transações
- Rollback em erros

✅ **Mapeamentos**
- DTO → Entity
- Entity → DTO
- Request → Model
- Model → Response

---

### 📈 Qualidade dos Testes

#### Boas Práticas Aplicadas

✅ **Nomenclatura Descritiva** - @DisplayName em todos os testes  
✅ **Padrão AAA** - Arrange-Act-Assert bem estruturado  
✅ **Testes Isolados** - Sem dependências entre testes  
✅ **Limpeza de Dados** - @BeforeEach para setup  
✅ **Assertions Claras** - AssertJ para legibilidade  
✅ **Cobertura de Edge Cases** - Cenários limites testados  
✅ **Mock Adequado** - Mockito usado corretamente  
✅ **Perfil de Teste** - application-test.yml dedicado

---

### 🔍 Relatórios de Cobertura

#### Visualizar Cobertura Detalhada

Após executar os testes com JaCoCo:

```bash
mvnw.cmd clean test jacoco:report
start target\site\jacoco\index.html
```

O relatório mostra:
- **Cobertura por classe** (linhas, branches, métodos)
- **Cobertura por pacote**
- **Código não coberto** (highlight em vermelho)
- **Métricas detalhadas**

#### Arquivos de Relatório

```
target/
├── site/jacoco/
│   ├── index.html              # Relatório principal
│   ├── jacoco.csv              # Métricas em CSV
│   └── jacoco.xml              # Para CI/CD
└── surefire-reports/
    ├── *.txt                   # Resumo dos testes
    └── TEST-*.xml              # Detalhes para CI/CD
```

---

### 🎓 Perfil de Teste

Os testes utilizam o perfil `test` com configurações específicas:

**application-test.yml:**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  h2:
    console:
      enabled: true
```

**Características:**
- ✅ Banco H2 em memória
- ✅ Schema criado/destruído automaticamente
- ✅ Sem impacto no banco de produção
- ✅ Testes isolados e rápidos

---

## 📋 Testes Manuais via API

### Testes via cURL

#### Criar Usuário
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Maria Silva",
    "email": "maria@example.com",
    "login": "mariasilva",
    "senha": "senha12345678"
  }'
```

#### Listar Todos
```bash
curl http://localhost:8080/api/users
```

#### Buscar por ID
```bash
curl http://localhost:8080/api/users/1
```

#### Atualizar
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "email": "maria.nova@example.com",
    "login": "marianova",
    "senha": "novasenha123"
  }'
```

#### Deletar
```bash
curl -X DELETE http://localhost:8080/api/users/1
```

### Cenários de Erro

#### Email Duplicado (409)
```bash
# Criar primeiro usuário
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"nome":"Pedro","email":"pedro@test.com","login":"pedro1","senha":"senha12345678"}'

# Tentar criar com mesmo email
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"nome":"Pedro2","email":"pedro@test.com","login":"pedro2","senha":"senha12345678"}'
```

#### Validação de Dados (400)
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"nome":"A","email":"invalido","login":"ab","senha":"123"}'
```

### Swagger UI
Acesse http://localhost:8080/swagger-ui.html para testar interativamente.

---

## 🐛 Troubleshooting

### Problema: Porta 8080 em uso

**Windows:**
```bash
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Linux/Mac:**
```bash
lsof -i :8080
kill -9 <PID>
```

**Solução alternativa:**
Mudar porta em `docker-compose.yml`:
```yaml
ports: ["8081:8080"]
```

### Problema: Container não inicia

```bash
# Ver logs
docker-compose logs user-health-bff

# Rebuild limpo
docker-compose down -v
docker system prune -f
docker-compose up -d --build
```

### Problema: PostgreSQL não conecta

```bash
# Verificar se banco está healthy
docker-compose ps

# Reiniciar banco
docker-compose restart app-db

# Ver logs do banco
docker-compose logs app-db
```

### Problema: Build Maven falha

```bash
# Limpar e rebuildar
./mvnw clean install -U

# Pular testes temporariamente
./mvnw clean install -DskipTests
```

### Problema: Lombok não funciona na IDE

- **IntelliJ IDEA:** File → Settings → Plugins → Lombok
- **Eclipse:** Instalar lombok.jar
- **VS Code:** Instalar extensão Lombok

---

## 🎯 Roadmap

### ✅ Implementado (v1.0)
- CRUD completo de usuários
- Autenticação JWT com RSA (RS256)
- Criptografia de senhas com BCrypt
- Spring Security com endpoints protegidos
- Validações de entrada (Bean Validation)
- Tratamento de exceções centralizado
- Documentação Swagger/OpenAPI completa
- Docker e Docker Compose (prod e local)
- Health checks e métricas
- Testes unitários e de integração

### 🔵 Curto Prazo (v1.1 - v1.2)
- [ ] Paginação e ordenação nos endpoints
- [ ] Filtros de busca avançados
- [ ] Cobertura de código 90%+
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Auditoria (created_at, updated_at)

### 🟢 Médio Prazo (v2.0)
- [ ] Cache com Redis
- [ ] Soft delete
- [ ] Rate limiting
- [ ] Integração com OpenTelemetry e Jaeger
- [ ] Métricas Prometheus customizadas

### 🟡 Longo Prazo (v3.0+)
- [ ] Event sourcing
- [ ] CQRS pattern
- [ ] Mensageria (Kafka/RabbitMQ)
- [ ] Observabilidade avançada
- [ ] LGPD/GDPR compliance

---

## 📊 Boas Práticas Implementadas

✅ **Arquitetura em camadas** (Controller/Service/Repository)  
✅ **DTOs** para entrada e saída (separação de concerns)  
✅ **Validação Bean Validation** (@Valid, @NotNull, @Email, etc)  
✅ **Tratamento centralizado de exceções** (GlobalExceptionHandler)  
✅ **Transações** com @Transactional  
✅ **Documentação OpenAPI/Swagger** completa e interativa  
✅ **Lombok** para reduzir boilerplate  
✅ **Design Patterns** (Builder, Repository, Strategy)  
✅ **Senha não exposta** nas respostas JSON  
✅ **Validação de email único** no banco  
✅ **Mensagens de erro padronizadas** (ApiErrorMessage)  
✅ **Docker multi-stage build** otimizado  
✅ **Health checks** em todos os containers  
✅ **Código limpo e manutenível**  
✅ **Autenticação JWT com RSA (RS256)**  
✅ **Senhas criptografadas com BCrypt (10 rounds)**  
✅ **Spring Security** configurado corretamente  
✅ **Endpoints públicos e protegidos** bem definidos  
✅ **CORS** configurado  
✅ **Session stateless** (escalável)  
✅ **Testes unitários e de integração** (JUnit 5 + Mockito)  

---

## 🆘 Suporte e Contato

### Documentação
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

### Links Úteis
- **Repositório:** [GitHub]
- **Issues:** [GitHub Issues]
- **Wiki:** [Confluence/Wiki]

### Equipe
- **Desenvolvido por:** Tech Challenge Team - FIAP
- **Versão:** 1.0
- **Data:** 09/02/2026

---

## 📄 Licença

Este projeto é parte de um desafio técnico educacional - FIAP Tech Challenge.

---

**🎉 README único e objetivo - Documentação completa da aplicação!**

_Desenvolvido com ❤️ pela equipe Tech Challenge_

