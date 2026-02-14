# 📐 DIAGRAMAS DE ARQUITETURA - Brain Health v2.0

## 🏗️ Visão Geral da Arquitetura

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │   Web App    │  │  Mobile App  │  │   Postman    │           │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘           │
│         │                 │                 │                    │
│         └─────────────────┴─────────────────┘                    │
│                           │                                      │
└───────────────────────────┼──────────────────────────────────────┘
                            │
                    HTTP REST API
                            │
┌───────────────────────────▼──────────────────────────────────────┐
│                      API LAYER (Controllers)                      │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │  AIArticleController                                      │   │
│  │  ├─ POST /api/v1/ai/articles/search                       │   │
│  │  └─ GET  /api/v1/ai/articles/health                       │   │
│  └───────────────────────────────────────────────────────────┘   │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │  APIExceptionHandler                                      │   │
│  │  ├─ ArticleNotFoundException → 404                        │   │
│  │  ├─ AIProcessingException → 500                           │   │
│  │  └─ ValidationException → 400                             │   │
│  └───────────────────────────────────────────────────────────┘   │
└───────────────────────────┬──────────────────────────────────────┘
                            │
                         DTOs
                            │
┌───────────────────────────▼──────────────────────────────────────┐
│                     DOMAIN LAYER (Business Logic)                 │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │  ArticleOrchestrationService (Orchestrator)               │   │
│  │  ├─ Validate input                                        │   │
│  │  ├─ Call repository to find article                       │   │
│  │  ├─ Validate article content                              │   │
│  │  ├─ Process with AI                                       │   │
│  │  └─ Map to response DTO                                   │   │
│  └───────────────────────────────────────────────────────────┘   │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │  AIProcessingService (AI Logic)                           │   │
│  │  ├─ Build prompt                                          │   │
│  │  ├─ Call OpenAI API                                       │   │
│  │  └─ Convert response                                      │   │
│  └───────────────────────────────────────────────────────────┘   │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │  MedicalArticle (Rich Domain Model)                       │   │
│  │  ├─ hasMinimumContent()                                   │   │
│  │  ├─ getSummarizedContent()                                │   │
│  │  ├─ isFromTrustedSource()                                 │   │
│  │  └─ getContentLength()                                    │   │
│  └───────────────────────────────────────────────────────────┘   │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │  MedicalArticleRepository (PORT - Interface)              │   │
│  │  └─ findByTopic(String) : Optional<MedicalArticle>        │   │
│  └───────────────────────────────────────────────────────────┘   │
└───────────────────────────┬──────────────────────────────────────┘
                            │
                      Dependency
                            │
┌───────────────────────────▼──────────────────────────────────────┐
│                  INFRASTRUCTURE LAYER (Adapters)                  │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │  CremespArticleAdapter (ADAPTER)                          │   │
│  │  implements MedicalArticleRepository                      │   │
│  │  ├─ Build search URL                                      │   │
│  │  ├─ Fetch HTML content                                    │   │
│  │  ├─ Parse with JSoup                                      │   │
│  │  ├─ Extract article text                                  │   │
│  │  └─ Return MedicalArticle                                 │   │
│  └───────────────────────────────────────────────────────────┘   │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │  OpenAI Integration (via Spring AI)                       │   │
│  │  ├─ ChatModel                                             │   │
│  │  └─ BeanOutputConverter                                   │   │
│  └───────────────────────────────────────────────────────────┘   │
└───────────────────────────┬──────────────────────────────────────┘
                            │
                      HTTP Calls
                            │
┌───────────────────────────▼──────────────────────────────────────┐
│                    EXTERNAL SERVICES                              │
│  ┌──────────────────┐              ┌──────────────────┐           │
│  │   CREMESP.org    │              │   OpenAI API     │           │
│  │   (Articles)     │              │   (GPT-4)        │           │
│  └──────────────────┘              └──────────────────┘           │
└───────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Fluxo de Requisição (Sequence Diagram)

```
Client          Controller         Orchestrator      Repository       AI Service      OpenAI
  │                  │                   │                │                │             │
  │  POST /search    │                   │                │                │             │
  ├─────────────────>│                   │                │                │             │
  │                  │                   │                │                │             │
  │                  │ searchAndGenerate │                │                │             │
  │                  ├──────────────────>│                │                │             │
  │                  │                   │                │                │             │
  │                  │                   │  findByTopic   │                │             │
  │                  │                   ├───────────────>│                │             │
  │                  │                   │                │                │             │
  │                  │                   │                │  HTTP GET      │             │
  │                  │                   │                ├───────────────────────────>  │
  │                  │                   │                │                │   HTML      │
  │                  │                   │                │<───────────────────────────  │
  │                  │                   │                │                │             │
  │                  │                   │  MedicalArticle│                │             │
  │                  │                   │<───────────────┤                │             │
  │                  │                   │                │                │             │
  │                  │                   │ hasMinimumContent()             │             │
  │                  │                   ├────────────┐   │                │             │
  │                  │                   │            │   │                │             │
  │                  │                   │<───────────┘   │                │             │
  │                  │                   │                │                │             │
  │                  │                   │     processWithAI               │             │
  │                  │                   ├────────────────────────────────>│             │
  │                  │                   │                │                │             │
  │                  │                   │                │                │  API Call   │
  │                  │                   │                │                ├────────────>│
  │                  │                   │                │                │  Response   │
  │                  │                   │                │                │<────────────│
  │                  │                   │                │                │             │
  │                  │                   │     ArticleResponse             │             │
  │                  │                   │<────────────────────────────────┤             │
  │                  │                   │                │                │             │
  │                  │                   │  toResponse    │                │             │
  │                  │                   ├────────────┐   │                │             │
  │                  │                   │            │   │                │             │
  │                  │                   │<───────────┘   │                │             │
  │                  │                   │                │                │             │
  │                  │  ArticleResponse  │                │                │             │
  │                  │<──────────────────┤                │                │             │
  │                  │                   │                │                │             │
  │   200 OK         │                   │                │                │             │
  │<─────────────────┤                   │                │                │             │
  │   JSON Response  │                   │                │                │             │
```

---

## 🧩 Camadas e Responsabilidades

```
┌─────────────────────────────────────────────────────────────┐
│                    API LAYER                                 │
│  Responsabilidade: Comunicação HTTP                          │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ • Recebe requisições HTTP                           │    │
│  │ • Valida DTOs de entrada                            │    │
│  │ • Chama domain services                             │    │
│  │ • Transforma respostas para JSON                    │    │
│  │ • Trata exceptions HTTP                             │    │
│  └─────────────────────────────────────────────────────┘    │
│  Regras:                                                     │
│  ✅ Pode: Usar Spring annotations, DTOs, HTTP codes         │
│  ❌ Não pode: Lógica de negócio, acesso direto a DB/APIs    │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                   DOMAIN LAYER                               │
│  Responsabilidade: Lógica de Negócio                         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ • Regras de negócio                                 │    │
│  │ • Orquestração de fluxos                            │    │
│  │ • Validações de domínio                             │    │
│  │ • Entidades ricas                                   │    │
│  │ • Interfaces (Ports)                                │    │
│  └─────────────────────────────────────────────────────┘    │
│  Regras:                                                     │
│  ✅ Pode: Usar Java puro, regras de negócio                 │
│  ❌ Não pode: Conhecer Spring, HTTP, DB, APIs externas       │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                INFRASTRUCTURE LAYER                          │
│  Responsabilidade: Detalhes Técnicos                         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ • Implementa Ports (adapters)                       │    │
│  │ • Acessa APIs externas                              │    │
│  │ • Persiste dados                                    │    │
│  │ • Integrações técnicas                              │    │
│  └─────────────────────────────────────────────────────┘    │
│  Regras:                                                     │
│  ✅ Pode: Spring, HTTP clients, DB drivers, libs externas    │
│  ❌ Não pode: Lógica de negócio                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 Estrutura de Pacotes (Tree View)

```
com.fiap.brain.health/
│
├── 🌐 api/                          # API Layer
│   ├── controller/
│   │   └── AIArticleController      # REST endpoints
│   ├── dto/
│   │   ├── request/
│   │   │   └── AIArticleRequest     # Input DTO
│   │   └── response/
│   │       ├── ArticleResponse      # Output DTO
│   │       ├── Recommendation       # Nested DTO
│   │       └── Quiz                 # Nested DTO
│   └── exception/
│       └── APIExceptionHandler      # Global error handler
│
├── 🧠 domain/                       # Domain Layer (Core)
│   ├── model/
│   │   └── MedicalArticle           # Rich domain entity
│   ├── service/
│   │   ├── ArticleOrchestrationService  # Orchestrator
│   │   └── AIProcessingService          # AI logic
│   ├── mapper/
│   │   └── ArticleResponseMapper    # DTO transformations
│   ├── repository/
│   │   └── MedicalArticleRepository # PORT (interface)
│   └── exception/
│       ├── DomainException          # Base exception
│       ├── ArticleNotFoundException
│       └── ArticleSearchException
│
├── 🔧 infrastructure/               # Infrastructure Layer
│   └── integration/
│       └── external/
│           └── CremespArticleAdapter  # ADAPTER (implementation)
│
├── ⚙️ config/                       # Configuration
│   ├── KafkaConsumerConfig
│   ├── KafkaProducerConfig
│   └── WebClientConfig
│
├── ⚠️ controller/ (deprecated)      # Old controllers
├── ⚠️ service/ (deprecated)         # Old services
└── ⚠️ dto/ia/ (deprecated)          # Old DTOs
```

---

## 🎯 Dependency Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ depends on
       ▼
┌─────────────┐
│    API      │ ───┐
└──────┬──────┘    │
       │           │ depends on
       ▼           │
┌─────────────┐    │
│   Domain    │◄───┘
└──────┬──────┘
       │ defines interfaces
       ▼
┌─────────────┐
│   Infra     │ ───┐
└─────────────┘    │ implements interfaces
                   │
                   ▼
            (Dependency Inversion)
```

**Regra:** Dependências apontam SEMPRE para dentro (para o Domain)

---

## 🔀 Hexagonal Architecture View

```
                    ┌─────────────────┐
                    │   Domain Core   │
                    │                 │
     ┌──────────────┤  Business Logic │──────────────┐
     │              │   (Pure Java)   │              │
     │              └─────────────────┘              │
     │                                               │
     │                                               │
┌────▼────┐                                     ┌────▼────┐
│  PORT   │                                     │  PORT   │
│  (IN)   │                                     │  (OUT)  │
└────┬────┘                                     └────┬────┘
     │                                               │
     │                                               │
┌────▼────────┐                              ┌───────▼──────┐
│  ADAPTER    │                              │   ADAPTER    │
│  REST API   │                              │   CREMESP    │
│ Controller  │                              │   External   │
└─────────────┘                              └──────────────┘
     ▲                                               │
     │ HTTP                                          │ HTTP
     │                                               ▼
┌────┴────┐                                  ┌──────────────┐
│ Client  │                                  │  CREMESP.org │
└─────────┘                                  └──────────────┘
```

**Portas (Ports):**
- **IN**: `AIArticleController` (recebe requests)
- **OUT**: `MedicalArticleRepository` (busca dados)

**Adaptadores (Adapters):**
- **IN**: `AIArticleController` (Spring REST)
- **OUT**: `CremespArticleAdapter` (HTTP client)

---

## 📊 Class Diagram (Principais Classes)

```
┌─────────────────────────────────┐
│  AIArticleController            │
├─────────────────────────────────┤
│ - orchestrationService          │
├─────────────────────────────────┤
│ + searchArticle()               │
│ + health()                      │
└────────────┬────────────────────┘
             │ uses
             ▼
┌─────────────────────────────────┐
│ ArticleOrchestrationService     │
├─────────────────────────────────┤
│ - articleRepository             │
│ - aiProcessingService           │
│ - responseMapper                │
├─────────────────────────────────┤
│ + searchAndGenerateArticle()    │
└─────┬───────────┬───────────────┘
      │           │
      │ uses      │ uses
      ▼           ▼
┌──────────────┐  ┌──────────────────┐
│   Repository │  │ AIProcessingServ │
│  «interface» │  ├──────────────────┤
├──────────────┤  │ - chatModel      │
│ + findBy     │  ├──────────────────┤
│   Topic()    │  │ + processWithAI()│
└──────▲───────┘  └──────────────────┘
       │
       │ implements
       │
┌──────┴──────────────────┐
│ CremespArticleAdapter   │
├─────────────────────────┤
│ - htmlFetchService      │
│ - baseUrl               │
│ - maxContentLength      │
├─────────────────────────┤
│ + findByTopic()         │
└─────────────────────────┘
```

---

## 🔍 Comparison: Before vs After

### BEFORE (v1.0)
```
Controller ──> Service ──> External API
    │            │
    │            └──> Has ALL logic:
    │                 - Search
    │                 - AI Processing
    │                 - Mapping
    │                 - Error handling
    │
    └──> Directly coupled to implementation
```

### AFTER (v2.0)
```
Controller ──> Orchestrator ──┬──> Repository ──> Adapter ──> External
                              │
                              ├──> AI Service ──> OpenAI
                              │
                              └──> Mapper ──> DTOs

✅ Separation of Concerns
✅ Dependency Inversion
✅ Easy to test
✅ Easy to replace implementations
```

---

**Diagramas criados por:** GitHub Copilot  
**Ferramenta:** ASCII Art / PlantUML-like  
**Data:** 2026-02-10
