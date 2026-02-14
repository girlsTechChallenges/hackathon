# 🧠 Brain Health - AI Medical Article Service

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20Hexagonal-blue.svg)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
[![Tests](https://img.shields.io/badge/Tests-179%20passed-success.svg)](RELATORIO_FINAL_TESTES.md)
[![Coverage](https://img.shields.io/badge/Coverage-85%25%2B-brightgreen.svg)](JACOCO_COVERAGE_GUIDE.md)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Microserviço de geração de conteúdo médico com IA, buscando artigos em fontes confiáveis e processando com OpenAI.

---

## 📋 Índice

- [Sobre](#sobre)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Configuração](#configuração)
- [Executando](#executando)
- [Testes](#testes)
- [API](#api)
- [Documentação](#documentação)

---

## 🎯 Sobre

O Brain Health é um microserviço que:

1. **Busca** artigos médicos em fontes confiáveis (CREMESP)
2. **Processa** o conteúdo com IA (OpenAI GPT-4)
3. **Gera** conteúdo estruturado com:
   - Introdução
   - Recomendações práticas
   - Conclusão
   - Quiz educativo

**Versão Atual:** 2.0.0 (Refatorado com Clean Architecture)

---

## 🏗️ Arquitetura

### Clean Architecture + Hexagonal

```
┌─────────────────────────────────────────────────┐
│              API Layer (Controllers)            │
│  ┌──────────────────────────────────────────┐   │
│  │  POST /api/v1/ai/articles/search         │   │
│  └──────────────────────────────────────────┘   │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│              Domain Layer (Core)                │
│  ┌──────────────────────────────────────────┐   │
│  │  ArticleOrchestrationService             │   │
│  │  AIProcessingService                     │   │
│  │  MedicalArticle (Rich Domain Model)      │   │
│  └──────────────────────────────────────────┘   │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│         Infrastructure Layer (Adapters)         │
│  ┌──────────────────────────────────────────┐   │
│  │  CremespArticleAdapter                   │   │
│  │  OpenAI Integration                      │   │
│  └──────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
```

### Princípios Aplicados

✅ **SOLID** - Todos os 5 princípios  
✅ **DDD** - Domain-Driven Design  
✅ **Ports & Adapters** - Hexagonal Architecture  
✅ **Clean Architecture** - Camadas bem definidas  

---

## 📦 Pré-requisitos

- **Java 21+** - [Download](https://adoptium.net/)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **OpenAI API Key** - [Get Key](https://platform.openai.com/api-keys)

---

## 🚀 Instalação

### 1. Clone o repositório

```bash
git clone https://github.com/your-org/brain-health.git
cd brain-health
```

### 2. Configure variáveis de ambiente

#### Windows (PowerShell)
```powershell
$env:OPENAI_API_KEY="sk-your-key-here"
```

#### Linux/Mac
```bash
export OPENAI_API_KEY="sk-your-key-here"
```

### 3. Build do projeto

```bash
mvn clean install
```

---

## ⚙️ Configuração

### application.properties

```properties
# Spring Application
spring.application.name=brain-health

# OpenAI Configuration
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4o-mini
spring.ai.openai.chat.options.temperature=0.7

# External Services - CREMESP
external-services.cremesp.base-url=https://cremesp.org.br/pesquisar.php
external-services.cremesp.max-content-length=8000
external-services.cremesp.timeout-seconds=30

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

---

## 🏃 Executando

### Modo Desenvolvimento

```bash
mvn spring-boot:run
```

### Modo Produção

```bash
java -jar target/brain-health-0.0.1-SNAPSHOT.jar
```

### Docker (Opcional)

```bash
docker build -t brain-health:2.0.0 .
docker run -p 8080:8080 \
  -e OPENAI_API_KEY=sk-your-key \
  brain-health:2.0.0
```

A aplicação estará disponível em: **http://localhost:8080**

### 🐳 Interfaces do Kafka para Teste

O projeto inclui três interfaces web para gerenciar e testar o Kafka:

#### Iniciar todas as interfaces:

**Windows:**
```bash
.\scripts\start-kafka-ui.bat
```

**Linux/Mac:**
```bash
./scripts/start-kafka-ui.sh
```

#### Ou manualmente:

```bash
docker-compose up -d
```

#### Interfaces disponíveis:

1. **Kafka UI (Provectus)** - http://localhost:8080
   - Interface moderna e completa
   - Criar tópicos, enviar mensagens, monitorar consumers

2. **Kafdrop** - http://localhost:9000
   - Interface leve e rápida
   - Visualizar tópicos e mensagens facilmente

3. **Kafka REST Proxy** - http://localhost:8082
   - API REST para produzir/consumir mensagens
   - Ideal para testes automatizados

📖 **Guia completo:** Consulte [KAFKA_TESTING_GUIDE.md](KAFKA_TESTING_GUIDE.md) para exemplos detalhados e instruções de uso.

---

## 🧪 Testes

### 📊 Estatísticas de Testes

| Tipo | Quantidade | Cobertura |
|------|------------|-----------|
| **Testes Unitários** | 93 | Domain + Application |
| **Testes de Integração** | 52 | Controllers + Full Stack |
| **Testes E2E** | 34 | Fluxos Completos |
| **TOTAL** | **179** | **85%+** |

### 🚀 Executar Testes

#### **Todos os testes:**
```bash
mvn test
```

#### **Apenas Unitários:**
```bash
mvn test -Dtest="*Test,!*IntegrationTest,!*E2ETest"
```

#### **Apenas Integração:**
```bash
mvn test -Dtest="*IntegrationTest"
```

#### **Apenas E2E:**
```bash
mvn test -Dtest="*E2ETest"
```

#### **Teste Específico:**
```bash
# Testes unitários do domain
mvn test -Dtest=MedicalArticleTest

# Testes do serviço de orquestração
mvn test -Dtest=SearchAndGenerateArticleUseCaseTest

# Testes E2E de busca de artigos
mvn test -Dtest=ArticleSearchE2ETest
```

### 📊 Cobertura de Código (JaCoCo)

#### **Gerar relatório de cobertura:**

##### Windows:
```bash
scripts\run-tests-with-coverage.bat
```

##### Linux/Mac:
```bash
chmod +x scripts/run-tests-with-coverage.sh
./scripts/run-tests-with-coverage.sh
```

##### Maven:
```bash
mvn clean test jacoco:report
# Relatório HTML: target/site/jacoco/index.html
# Relatório XML: target/site/jacoco/jacoco.xml
```

#### **Verificar mínimos de cobertura:**
```bash
mvn jacoco:check
# Mínimo configurado: 80% linhas, 70% branches
```

### 📚 Documentação de Testes

- 📖 [**Relatório Completo de Testes**](RELATORIO_FINAL_TESTES.md) - 179 testes implementados
- 📊 [**Guia de Cobertura JaCoCo**](JACOCO_COVERAGE_GUIDE.md) - Como usar e interpretar
- 🧪 [**Testes Unitários**](TESTES_UNITARIOS_RESUMO.md) - 93 testes (Domain + Application)
- 🔗 [**Testes de Integração**](TESTES_INTEGRACAO_RESUMO.md) - 52 testes (Controllers + Full Stack)
- 🌐 [**Testes E2E**](TESTES_E2E_RESUMO.md) - 34 testes (Fluxos Completos)

### 🎯 Estrutura de Testes

```
src/test/java/
├── domain/model/
│   └── MedicalArticleTest.java (24 testes)
├── application/
│   ├── mapper/ArticleResponseMapperTest.java (25 testes)
│   └── usecase/
│       ├── SearchAndGenerateArticleUseCaseTest.java (21 testes)
│       └── ProcessKafkaMessageUseCaseTest.java (23 testes)
├── api/controller/
│   ├── AIArticleControllerIntegrationTest.java (19 testes)
│   └── KafkaControllerIntegrationTest.java (18 testes)
├── integration/
│   └── FullStackIntegrationTest.java (15 testes)
└── e2e/
    ├── ArticleSearchE2ETest.java (17 testes)
    └── KafkaMessagingE2ETest.java (17 testes)
```

---

## 📡 API

### Endpoint Principal (Novo - Recomendado)

#### POST /api/v1/ai/articles/search

Busca artigo e gera conteúdo com IA.

**Request:**
```json
{
  "message": "Quais são os benefícios da meditação para saúde mental?"
}
```

**Response (200 OK):**
```json
{
  "title": "Benefícios da Meditação para Saúde Mental",
  "introduction": "A meditação é uma prática milenar...",
  "recommendations": [
    {
      "category": "Prática Diária",
      "description": "Medite por 10-15 minutos diariamente",
      "tips": [
        "Escolha um horário fixo",
        "Encontre local tranquilo",
        "Use aplicativos guiados"
      ]
    }
  ],
  "conclusion": "A meditação traz benefícios comprovados...",
  "context": "Saúde Mental",
  "quizzes": [
    {
      "question": "Qual o tempo mínimo recomendado de meditação diária?",
      "options": ["5 minutos", "10-15 minutos", "30 minutos", "1 hora"],
      "correctAnswer": "10-15 minutos"
    }
  ],
  "sourceLink": "https://cremesp.org.br/article/123",
  "timestamp": "2026-02-10 14:30:00"
}
```

**Erros:**

- `400 Bad Request` - Request inválido
- `404 Not Found` - Artigo não encontrado
- `429 Too Many Requests` - Quota OpenAI excedida
- `500 Internal Server Error` - Erro no processamento

### Endpoint Legado (Deprecated)

#### POST /api/ai/article ⚠️

> **DEPRECATED:** Use `/api/v1/ai/articles/search` em vez deste.  
> Será removido na versão 3.0.0

---

## 📚 Documentação

### Arquitetura e Migrações

- **[REFACTORING_SUMMARY.md](REFACTORING_SUMMARY.md)** - Resumo completo da refatoração
- **[MIGRATION_GUIDE.md](MIGRATION_GUIDE.md)** - Guia de migração detalhado

### Estrutura do Projeto

```
brain-health/
├── src/main/java/com/fiap/brain/health/
│   ├── api/                    # Camada de apresentação
│   │   ├── controller/         # REST Controllers
│   │   ├── dto/                # DTOs de request/response
│   │   └── exception/          # Exception handlers
│   │
│   ├── domain/                 # Lógica de negócio
│   │   ├── model/              # Domain models
│   │   ├── service/            # Business services
│   │   ├── mapper/             # Mappers
│   │   ├── repository/         # Repository interfaces (Ports)
│   │   └── exception/          # Domain exceptions
│   │
│   ├── infrastructure/         # Detalhes de implementação
│   │   └── integration/
│   │       └── external/       # Adapters externos
│   │
│   └── config/                 # Configurações
│
├── src/test/java/              # Testes
├── src/main/resources/         # Recursos
├── pom.xml                     # Maven dependencies
└── README.md                   # Este arquivo
```

---

## 🔧 Tecnologias

- **Java 21** - Linguagem
- **Spring Boot 3.3.5** - Framework
- **Spring AI** - Integração OpenAI
- **OpenAI GPT-4** - Processamento IA
- **JSoup** - Parsing HTML
- **Lombok** - Redução boilerplate
- **JUnit 5** - Testes unitários
- **Mockito** - Mocks em testes

---

## 📊 Qualidade do Código

### Métricas (Versão 2.0)

- ✅ Cobertura de testes: 75%+
- ✅ Complexidade ciclomática: < 10
- ✅ Acoplamento: Baixo (Hexagonal)
- ✅ Coesão: Alta (Single Responsibility)
- ✅ Nomenclatura: Inglês correto

### SonarQube (Target)

- Code Smells: 0
- Bugs: 0
- Vulnerabilities: 0
- Technical Debt: < 5%

---

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

### Padrões de Código

- Seguir Clean Code principles
- Manter cobertura de testes > 70%
- Documentar APIs com JavaDoc
- Seguir convenções Java (Google Style Guide)

---

## 📝 Changelog

### [2.0.0] - 2026-02-10

#### ✨ Adicionado
- Nova arquitetura Clean + Hexagonal
- Domain model rico (MedicalArticle)
- Ports & Adapters pattern
- Exception handling estruturado
- Testes unitários completos
- Configurações externalizadas

#### ♻️ Modificado
- Nomenclatura corrigida (Inglês)
- Separação de responsabilidades
- Organização de pacotes

#### ⚠️ Deprecated
- `IntelligenceArtificialController`
- `IntelligenceArtificialService`
- Endpoint `/api/ai/article`

### [1.0.0] - 2026-01-15

- Release inicial

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 👥 Autores

- **FIAP Tech Challenge Team** - *Desenvolvimento inicial*
- **GitHub Copilot** - *Refatoração arquitetural 2.0*

---

## 🙏 Agradecimentos

- CREMESP - Fonte de artigos médicos
- OpenAI - Processamento de IA
- Spring Team - Framework excelente
- Clean Architecture Community

---

## 📞 Suporte

- **Issues:** [GitHub Issues](https://github.com/your-org/brain-health/issues)
- **Email:** support@brainhealth.com
- **Docs:** [Documentação Completa](https://docs.brainhealth.com)

---

**Desenvolvido com ❤️ e ☕ pela FIAP Tech Challenge Team**
