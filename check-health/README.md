# 🏥 Check Health - API de Metas de Saúde Gamificada

API REST para criação e acompanhamento de metas de saúde com sistema de gamificação, desenvolvida como parte do Tech Challenge da FIAP.

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Endpoints da API](#endpoints-da-api)
- [Cobertura de Testes](#cobertura-de-testes)
- [Como Executar](#como-executar)
- [Documentação](#documentação)

## 📖 Sobre o Projeto

O **Check Health** é um microserviço que gerencia metas de saúde pessoais com elementos de gamificação. Permite aos usuários criar, acompanhar e atualizar metas relacionadas a diferentes categorias de saúde, recebendo pontos e badges como recompensa pelo progresso.

### Funcionalidades Principais

- ✅ CRUD completo de metas de saúde
- 📊 Acompanhamento de progresso em tempo real
- 🎮 Sistema de gamificação (pontos e badges)
- 🔔 Suporte para notificações
- 📅 Metas por período (diária, semanal, mensal, pontual)
- 🏆 Níveis de dificuldade (fácil, média, difícil)
- 📢 Publicação de eventos via Kafka

### Categorias Suportadas

- 💪 **SAUDE_FISICA** - Atividades físicas e exercícios
- 🧠 **SAUDE_MENTAL** - Bem-estar mental e emocional
- 🥗 **NUTRICAO** - Alimentação saudável
- 😴 **SONO** - Qualidade do sono
- 🌟 **BEM_ESTAR** - Bem-estar geral

## 🛠 Tecnologias

### Backend

- **Java 17** - Linguagem de programação
- **Spring Boot 4.0.2** - Framework principal
  - Spring Web - API REST
  - Spring Data JPA - Persistência de dados
  - Spring Kafka - Mensageria assíncrona
  - Spring Validation - Validação de dados
  - Spring Actuator - Monitoramento e métricas
  - Spring DevTools - Desenvolvimento
  - Spring Docker Compose - Integração com containers

### Banco de Dados

- **PostgreSQL 15** - Banco de dados relacional
- **H2 Database** - Testes em memória

### Mensageria

- **Apache Kafka 7.4.0** - Streaming de eventos
- **Zookeeper 7.4.0** - Coordenação de cluster Kafka

### Documentação e Code Generation

- **OpenAPI 3.0.3** - Especificação da API
- **SpringDoc OpenAPI 2.6.0** - Documentação interativa
- **OpenAPI Generator 7.2.0** - Geração de código

### Utilities

- **Lombok** - Redução de boilerplate
- **Jackson** - Serialização JSON

### Testes

- **JUnit 5** - Framework de testes
- **AssertJ** - Assertions fluentes
- **Testcontainers 1.19.3** - Containers para testes de integração
- **Spring Kafka Test** - Testes com Kafka
- **JaCoCo 0.8.8** - Cobertura de código

### DevOps

- **Docker** - Containerização
- **Docker Compose** - Orquestração de containers
- **Maven** - Gerenciamento de dependências e build

## 🏗 Arquitetura

O projeto segue uma **arquitetura em camadas** com **Event-Driven Architecture** para comunicação assíncrona.

### Padrões Arquiteturais

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Controllers  │  │   OpenAPI    │  │   Swagger    │      │
│  │   (REST)     │  │  Generated   │  │      UI      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      Business Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Services   │  │    Mappers   │  │  Validators  │      │
│  │  (Business   │  │    (DTO/     │  │              │      │
│  │    Logic)    │  │   Entity)    │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Persistence Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Repositories │  │   Entities   │  │      JPA     │      │
│  │  (Spring     │  │  (Domain     │  │   Hibernate  │      │
│  │    Data)     │  │   Model)     │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
                    ┌───────────────┐
                    │  PostgreSQL   │
                    │   Database    │
                    └───────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     Event Publishing                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │     Kafka    │  │    Events    │  │   Topics     │      │
│  │  Publishers  │  │     (DTOs)   │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
                    ┌───────────────┐
                    │  Apache Kafka │
                    │    Cluster    │
                    └───────────────┘
```

### Princípios Aplicados

- **Separation of Concerns** - Separação clara de responsabilidades
- **Dependency Injection** - Inversão de controle via Spring
- **Contract-First** - API definida via OpenAPI
- **Event-Driven** - Comunicação assíncrona via eventos
- **Repository Pattern** - Abstração de acesso a dados
- **DTO Pattern** - Transferência de dados entre camadas

### Fluxo de Dados

1. **Request** → Controller recebe requisição HTTP
2. **Validation** → Valida dados de entrada
3. **Business Logic** → Service processa regras de negócio
4. **Persistence** → Repository persiste no banco
5. **Event Publishing** → Publisher envia evento para Kafka
6. **Response** → Controller retorna resposta HTTP

## 📁 Estrutura do Projeto

```
check-health/
│
├── src/
│   ├── main/
│   │   ├── java/com/fiap/check/health/
│   │   │   ├── api/                      # Interfaces e modelos gerados pelo OpenAPI
│   │   │   │   └── model/               # DTOs de Request/Response
│   │   │   ├── config/                  # Configurações do Spring
│   │   │   │   ├── Config.java
│   │   │   │   └── KafkaConfig.java
│   │   │   ├── controller/              # Controllers REST
│   │   │   │   ├── GoalController.java
│   │   │   │   └── RootController.java
│   │   │   ├── dto/                     # DTOs de eventos
│   │   │   │   └── event/
│   │   │   │       ├── GoalCreatedEvent.java
│   │   │   │       └── GoalProgressUpdatedEvent.java
│   │   │   ├── event/                   # Publishers de eventos
│   │   │   │   └── publisher/
│   │   │   │       └── GoalEventPublisher.java
│   │   │   ├── exception/               # Exceções customizadas
│   │   │   │   ├── ApiErrorMessage.java
│   │   │   │   ├── GoalNotFoundException.java
│   │   │   │   ├── GoalAlreadyInProgressException.java
│   │   │   │   ├── GoalAlreadyCompletedException.java
│   │   │   │   └── GoalAlreadyCanceledException.java
│   │   │   ├── mapper/                  # Mapeamento Entity ↔ DTO
│   │   │   │   └── GoalMapper.java
│   │   │   ├── model/                   # Enums e Value Objects
│   │   │   │   ├── GoalCategory.java
│   │   │   │   ├── Frequency.java
│   │   │   │   ├── Reward.java
│   │   │   │   └── Progress.java
│   │   │   ├── persistence/             # Camada de persistência
│   │   │   │   ├── entity/
│   │   │   │   │   └── Goal.java
│   │   │   │   └── repository/
│   │   │   │       └── GoalRepository.java
│   │   │   ├── service/                 # Serviços e regras de negócio
│   │   │   │   ├── GoalService.java
│   │   │   │   └── impl/
│   │   │   │       └── GoalServiceImpl.java
│   │   │   └── CheckHealthMain.java     # Classe principal
│   │   │
│   │   └── resources/
│   │       └── application.yml          # Configurações da aplicação
│   │
│   └── test/
│       ├── java/com/fiap/check/health/
│       │   ├── api/model/              # Testes de modelos da API
│       │   ├── controller/             # Testes de controllers
│       │   ├── event/publisher/        # Testes de publishers
│       │   ├── exception/              # Testes de exceções
│       │   ├── integration/            # Testes de integração
│       │   ├── mapper/                 # Testes de mappers
│       │   ├── repository/             # Testes de repositories
│       │   ├── service/impl/           # Testes de services
│       │   └── util/                   # Utilitários de teste
│       │
│       └── resources/
│           └── application-test.yml    # Configurações de teste
│
├── target/                             # Artefatos de build
│   ├── classes/                        # Classes compiladas
│   ├── generated-sources/              # Código gerado pelo OpenAPI
│   ├── surefire-reports/              # Relatórios de testes
│   └── jacoco.exec                    # Dados de cobertura
│
├── check-health-api.yml               # Especificação OpenAPI 3.0
├── compose.yaml                       # Docker Compose configuration
├── KAFKA.md                           # Documentação do Kafka
├── pom.xml                            # Maven POM
├── README-TESTES.md                   # Documentação dos testes
└── README.md                           # Este arquivo
```

### Descrição das Camadas

#### 🎯 Controller Layer
- Exposição de endpoints REST
- Validação de entrada
- Tratamento de exceções HTTP
- Implementa interfaces geradas pelo OpenAPI

#### 💼 Service Layer
- Lógica de negócio
- Cálculos de gamificação
- Orquestração de operações
- Publicação de eventos

#### 🗄 Persistence Layer
- Repositórios Spring Data JPA
- Entidades JPA
- Queries customizadas

#### 🔄 Mapper Layer
- Conversão entre DTOs e Entities
- Transformação de dados

#### 📢 Event Layer
- Publicação de eventos no Kafka
- Serialização de eventos
- Logging de publicações

## 🔌 Endpoints da API

### Base URL
```
http://localhost:8080
```

### Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

### Endpoints Disponíveis

#### 1. Criar Meta
```http
POST /goals
Content-Type: application/json

{
  "user_id": "12345",
  "title": "Caminhar 30 minutos diariamente",
  "description": "Meta para aumentar atividade física diária",
  "category": "SAUDE_FISICA",
  "type": "daily",
  "start_date": "2026-02-08",
  "end_date": "2026-03-08",
  "frequency": {
    "periodicity": "daily",
    "times_per_period": 1
  },
  "difficulty": "easy",
  "reward": {
    "points": 50,
    "badge": "walker"
  },
  "status": "active",
  "notifications": true
}
```

**Response:** `201 Created`
```json
{
  "goal_id": "1",
  "user_id": "12345",
  "title": "Caminhar 30 minutos diariamente",
  "status": "active",
  "created_at": "2026-02-09T10:30:00",
  "progress": {
    "current": 0,
    "total": 30,
    "percentage": 0.0
  },
  "gamification": {
    "points_earned": 0,
    "total_points": 50,
    "badges": []
  }
}
```

#### 2. Listar Todas as Metas
```http
GET /goals
```

**Response:** `200 OK`
```json
[
  {
    "goal_id": "1",
    "user_id": "12345",
    "title": "Caminhar 30 minutos diariamente",
    "status": "active",
    ...
  }
]
```

#### 3. Buscar Meta por ID
```http
GET /goals/{goal_id}
```

**Response:** `200 OK` ou `404 Not Found`

#### 4. Atualizar Meta
```http
PUT /goals/{goal_id}
Content-Type: application/json

{
  "user_id": "12345",
  "title": "Caminhar 45 minutos diariamente",
  ...
}
```

**Response:** `200 OK`

#### 5. Deletar Meta
```http
DELETE /goals/{goal_id}
```

**Response:** `204 No Content`

#### 6. Atualizar Progresso
```http
PATCH /goals/{goal_id}/progress
Content-Type: application/json

{
  "increment": 10,
  "notes": "Caminhada realizada no parque"
}
```

**Response:** `200 OK`
```json
{
  "goal_id": "1",
  "progress": {
    "current": 10,
    "total": 30,
    "percentage": 33.33
  },
  "gamification": {
    "points_earned": 16,
    "total_points": 50,
    "badges": ["iniciante"]
  }
}
```

### Códigos de Status HTTP

| Código | Descrição |
|--------|-----------|
| 200 | OK - Requisição bem-sucedida |
| 201 | Created - Recurso criado com sucesso |
| 204 | No Content - Recurso deletado |
| 400 | Bad Request - Dados inválidos |
| 404 | Not Found - Recurso não encontrado |
| 500 | Internal Server Error - Erro no servidor |

## 🧪 Cobertura de Testes

O projeto possui uma extensa suíte de testes que garante a qualidade e confiabilidade do código.

### Tipos de Testes

#### 🔹 Testes Unitários
- **GoalServiceImplTest** - Testes do serviço de metas
- **GoalServiceImplBranchCoverageTest** - Cobertura de branches
- **GoalServiceImplAdvancedTest** - Cenários avançados
- **GoalMapperTest** - Testes de mapeamento
- **GoalControllerTest** - Testes do controller
- **GoalEventPublisherTest** - Testes de publicação de eventos
- **ExceptionTest** - Testes de exceções
- **ApiSubModelsTest** - Testes de modelos da API

#### 🔹 Testes de Integração
- **GoalControllerIntegrationTest** - Integração Controller → Service → Repository
- **GoalRepositorySimpleTest** - Testes do repository com banco real
- **GoalRepositoryAdvancedTest** - Queries complexas
- **GoalEventPublisherIntegrationTest** - Integração com Kafka
- **GoalRestApiIntegrationTest** - Testes da API REST completa

#### 🔹 Testes End-to-End
- **GoalEndToEndIntegrationTest** - Fluxo completo incluindo eventos Kafka

### Estrutura de Testes

```
✓ Testes de Modelos (DTOs)
  ├─ GoalRequestTest
  ├─ GoalRequestFrequencyTest
  ├─ GoalRequestRewardTest
  ├─ GoalResponseTest
  ├─ GoalResponseProgressTest
  └─ GoalResponseGamificationTest

✓ Testes de Controller
  ├─ Criação de metas
  ├─ Listagem de metas
  ├─ Busca por ID
  ├─ Atualização
  ├─ Deleção
  └─ Atualização de progresso

✓ Testes de Service
  ├─ Regras de negócio
  ├─ Cálculos de gamificação
  ├─ Validações
  └─ Tratamento de exceções

✓ Testes de Repository
  ├─ Persistência
  ├─ Queries customizadas
  ├─ Consultas por categoria
  └─ Consultas por usuário

✓ Testes de Eventos
  ├─ Publicação no Kafka
  ├─ Serialização de eventos
  └─ Confirmação de envio
```

### Ferramentas de Teste

- **JUnit 5** - Framework principal
- **Mockito** - Mocks e stubs
- **MockMvc** - Testes de API REST
- **AssertJ** - Assertions expressivas
- **Testcontainers** - PostgreSQL e Kafka em containers
- **H2 Database** - Banco em memória para testes rápidos
- **JaCoCo** - Análise de cobertura de código

### Cobertura de Código

O projeto utiliza **JaCoCo** para medir a cobertura de testes:

```bash
mvn test jacoco:report
```

Os relatórios são gerados em:
```
target/site/jacoco/index.html
```

### Executar Testes

```bash
# Todos os testes
mvn test

# Apenas testes unitários
mvn test -Dtest="*Test"

# Apenas testes de integração
mvn test -Dtest="*IntegrationTest"

# Teste específico
mvn test -Dtest=GoalControllerIntegrationTest

# Com relatório de cobertura
mvn test jacoco:report
```

## 🚀 Como Executar

### Pré-requisitos

- ☕ Java 17 ou superior
- 🐘 Maven 3.9+
- 🐳 Docker e Docker Compose
- 🔧 Git (para clonar o repositório)

### 1. Clonar o Repositório

```bash
git clone https://github.com/seu-usuario/check-health.git
cd check-health
```

### 2. Iniciar Infraestrutura (Docker)

```bash
# Iniciar PostgreSQL, Zookeeper e Kafka
docker compose up -d

# Verificar se os containers estão rodando
docker ps
```

### 3. Executar a Aplicação

#### Via Maven Wrapper

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

#### Via Maven Instalado

```bash
mvn spring-boot:run
```

#### Gerando JAR

```bash
# Compilar e gerar JAR
mvn clean package -DskipTests

# Executar JAR
java -jar target/check-health-0.0.1-SNAPSHOT.jar
```

### 4. Acessar a Aplicação

- **API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **Actuator Health:** http://localhost:8080/actuator/health
- **Actuator Metrics:** http://localhost:8080/actuator/metrics

### 5. Parar a Aplicação

```bash
# Parar containers
docker compose down

# Parar containers e remover volumes
docker compose down -v
```

## 📚 Documentação

### Arquivos de Documentação

- **[README.md](README.md)** - Este arquivo
- **[README-TESTES.md](README-TESTES.md)** - Documentação detalhada dos testes
- **[KAFKA.md](KAFKA.md)** - Documentação do Kafka e eventos
- **[check-health-api.yml](check-health-api.yml)** - Especificação OpenAPI 3.0

### Swagger/OpenAPI

A documentação interativa da API está disponível através do Swagger UI:

```
http://localhost:8080/swagger-ui/index.html
```

Você pode:
- ✅ Visualizar todos os endpoints
- ✅ Ver esquemas de Request e Response
- ✅ Testar endpoints diretamente pelo navegador
- ✅ Baixar a especificação OpenAPI

### Configurações

#### application.yml

```yaml
spring:
  application:
    name: check-health
  datasource:
    url: jdbc:postgresql://localhost:5432/checkhealth
    username: admin
    password: admin123
  jpa:
    hibernate:
      ddl-auto: update
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

server:
  port: 8080

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

## 🎯 Tópicos Kafka

### Eventos Publicados

| Tópico | Evento | Descrição |
|--------|--------|-----------|
| `goal.created` | GoalCreatedEvent | Disparado quando uma nova meta é criada |
| `goal.progress.updated` | GoalProgressUpdatedEvent | Disparado quando o progresso é atualizado |

### Estrutura dos Eventos

#### GoalCreatedEvent
```json
{
  "goalId": 1,
  "userId": "12345",
  "category": "SAUDE_FISICA",
  "title": "Caminhar 30 minutos diariamente",
  "description": "Meta para aumentar atividade física diária"
}
```

#### GoalProgressUpdatedEvent
```json
{
  "goalId": 1,
  "userId": "12345",
  "currentProgress": 10,
  "totalRequired": 30,
  "percentageCompleted": 33.33,
  "pointsEarned": 16
}
```

## 👥 Autores

Projeto desenvolvido como parte do Tech Challenge da FIAP.

## 📄 Licença

Este projeto foi desenvolvido para fins educacionais.

---

⭐ **Check Health** - Transformando saúde em conquistas! 🏆
