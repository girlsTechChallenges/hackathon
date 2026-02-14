# 📦 Arquivos Docker Compose Unificados - Resumo

Este documento lista todos os arquivos criados para facilitar o deployment da plataforma Health Tech com Docker Compose unificado.

---

## ✅ Arquivos Criados

### 🐳 Docker Compose

| Arquivo | Descrição | Uso |
|---------|-----------|-----|
| [`docker-compose.yml`](docker-compose.yml) | Compose unificado com 3 projetos | `docker-compose up -d --build` |
| [`.gitignore`](.gitignore) | Ignore de arquivos sensíveis e temporários | Automático pelo Git |
| [`brain-health/open_ai_api.env.example`](brain-health/open_ai_api.env.example) | Exemplo de configuração da API key | Copiar e editar |

### 📜 Scripts de Automação

| Arquivo | Descrição | Platform | Uso |
|---------|-----------|----------|-----|
| [`start-platform.sh`](start-platform.sh) | Script de inicialização | Linux/Mac | `./start-platform.sh` |
| [`start-platform.bat`](start-platform.bat) | Script de inicialização | Windows | `.\start-platform.bat` |
| [`stop-platform.sh`](stop-platform.sh) | Script para parar serviços | Linux/Mac | `./stop-platform.sh` |
| [`stop-platform.bat`](stop-platform.bat) | Script para parar serviços | Windows | `.\stop-platform.bat` |

### 📚 Documentação

| Arquivo | Descrição | Conteúdo |
|---------|-----------|----------|
| [`DOCKER-COMPOSE-GUIDE.md`](DOCKER-COMPOSE-GUIDE.md) | Guia completo do docker-compose | Como usar, troubleshooting, comandos |
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | Diagrama visual da arquitetura | ASCII art, fluxos, recursos |
| [`DEPLOYMENT-CHECKLIST.md`](DEPLOYMENT-CHECKLIST.md) | Checklist de validação | Testes passo a passo |
| [`README.md`](README.md) | README principal (atualizado) | Quick start com compose unificado |

---

## 🎯 Como Usar

### Primeira Vez (Setup Completo)

1. **Configure a OpenAI API Key:**
   ```bash
   # Opção 1: Usar o script (recomendado)
   ./start-platform.sh    # Linux/Mac
   .\start-platform.bat   # Windows
   
   # Opção 2: Manual
   echo "OPENAI_API_KEY=sk-your-key" > brain-health/open_ai_api.env
   ```

2. **Suba a plataforma:**
   ```bash
   # Opção 1: Usando script
   ./start-platform.sh    # Linux/Mac
   .\start-platform.bat   # Windows
   
   # Opção 2: Docker Compose direto
   docker-compose up -d --build
   ```

3. **Valide o deployment:**
   - Siga o checklist em [`DEPLOYMENT-CHECKLIST.md`](DEPLOYMENT-CHECKLIST.md)
   - Acesse os Swagger UIs
   - Execute os testes de integração

### Uso Diário

```bash
# Iniciar
docker-compose up -d

# Ver status
docker-compose ps

# Ver logs
docker-compose logs -f

# Parar (mantém dados)
docker-compose stop

# Parar e limpar (remove containers, mantém volumes)
docker-compose down
```

---

## 🏗️ Arquitetura

O docker-compose unificado cria:

### 11 Containers

1. **user-health-bff** - API de usuários (porta 8080)
2. **user-health-db** - PostgreSQL 16 (porta 5432)
3. **check-health-app** - API de metas (porta 8081)
4. **check-health-db** - PostgreSQL 15 (porta 5433)
5. **brain-health-app** - IA médica (porta 9090)
6. **kafka** - Message broker (porta 9092)
7. **zookeeper** - Coordenação Kafka (porta 2181)
8. **kafka-ui** - Interface Kafka (porta 8085)
9. **kafdrop** - Interface Kafka alternativa (porta 9000)
10. **kafka-rest-proxy** - API REST Kafka (porta 8082)

### 1 Network

- **health-platform-network** - Network bridge compartilhada

### 5 Volumes Persistentes

- **user-health-postgres-data** - Dados do User Health
- **check-health-postgres-data** - Dados do Check Health
- **kafka-data** - Mensagens Kafka
- **zookeeper-data** - Configuração Zookeeper
- **zookeeper-logs** - Logs Zookeeper

---

## 🔌 Portas Utilizadas

| Serviço | Porta | URL |
|---------|-------|-----|
| User Health BFF | 8080 | http://localhost:8080 |
| Check Health | 8081 | http://localhost:8081 |
| Brain Health | 9090 | http://localhost:9090 |
| User Health DB | 5432 | localhost:5432 |
| Check Health DB | 5433 | localhost:5433 |
| Kafka | 9092 | localhost:9092 |
| Zookeeper | 2181 | localhost:2181 |
| Kafka UI | 8085 | http://localhost:8085 |
| Kafdrop | 9000 | http://localhost:9000 |
| REST Proxy | 8082 | http://localhost:8082 |

---

## 🔑 Credenciais

### User Health DB (PostgreSQL)
- **Host:** localhost:5432
- **Database:** postgres
- **User:** postgres
- **Password:** postgres

### Check Health DB (PostgreSQL)
- **Host:** localhost:5433
- **Database:** checkhealth
- **User:** admin
- **Password:** admin123

### OpenAI
- **Config:** `brain-health/open_ai_api.env`
- **Var:** `OPENAI_API_KEY`

---

## 🎯 Diferenças vs Docker Compose Individual

### Antes (Individual)

```bash
# Subir User Health BFF
cd user-health-bff
docker-compose up -d

# Subir Check Health
cd ../check-health
docker-compose up -d

# Subir Brain Health
cd ../brain-health
docker-compose up -d
```

**Problemas:**
- ❌ Conflito de portas (8080, 5432, 9092)
- ❌ 3 clusters Kafka diferentes
- ❌ Redes isoladas
- ❌ Precisa gerenciar 3 docker-compose separados

### Agora (Unificado)

```bash
# Subir tudo de uma vez
docker-compose up -d --build
```

**Vantagens:**
- ✅ Um único comando
- ✅ Portas ajustadas automaticamente (8080, 8081, 9090)
- ✅ PostgreSQL independentes (5432, 5433)
- ✅ Um único cluster Kafka compartilhado
- ✅ Network compartilhada (comunicação entre serviços)
- ✅ Scripts de automação incluídos

---

## 📊 Mudanças de Porta

Para evitar conflitos, as portas foram ajustadas:

| Serviço | Antes | Agora | Motivo |
|---------|-------|-------|--------|
| Check Health API | 8080 | 8081 | Conflito com User Health |
| Check Health DB | 5432 | 5433 | Conflito com User Health DB |
| Kafka | 9092 (múltiplos) | 9092 (único) | Compartilhado entre serviços |

---

## 🧪 Testes Rápidos

### 1. Health Checks

```bash
curl http://localhost:8080/actuator/health  # User Health
curl http://localhost:8081/actuator/health  # Check Health
curl http://localhost:9090/actuator/health  # Brain Health
```

### 2. Criar Usuário

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"nome":"Test","email":"test@test.com","login":"test","senha":"senha123456"}'
```

### 3. Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"senha123456"}'
```

### 4. Criar Meta

```bash
curl -X POST http://localhost:8081/goals \
  -H "Content-Type: application/json" \
  -d '{"user_id":"1","title":"Caminhar","category":"SAUDE_FISICA","type":"daily","difficulty":"easy"}'
```

### 5. Gerar Conteúdo IA

```bash
curl -X POST http://localhost:9090/api/v1/ai/articles/search \
  -H "Content-Type: application/json" \
  -d '{"message":"Benefícios da caminhada"}'
```

---

## 🔧 Manutenção

### Ver Logs de Todos os Serviços

```bash
docker-compose logs -f
```

### Ver Logs de Um Serviço

```bash
docker-compose logs -f user-health-bff
docker-compose logs -f check-health-app
docker-compose logs -f brain-health-app
```

### Reiniciar Um Serviço

```bash
docker-compose restart user-health-bff
```

### Rebuild Um Serviço

```bash
docker-compose up -d --build user-health-bff
```

### Ver Recursos Consumidos

```bash
docker stats
```

---

## 📖 Documentação Completa

Para mais detalhes, consulte:

1. **[DOCKER-COMPOSE-GUIDE.md](DOCKER-COMPOSE-GUIDE.md)**
   - Guia de uso completo
   - Troubleshooting
   - Comandos avançados

2. **[ARCHITECTURE.md](ARCHITECTURE.md)**
   - Diagrama visual ASCII
   - Fluxos de comunicação
   - Recursos estimados

3. **[DEPLOYMENT-CHECKLIST.md](DEPLOYMENT-CHECKLIST.md)**
   - Checklist passo a passo
   - Validação de todos os serviços
   - Testes de integração

4. **[README.md](README.md)**
   - Documentação geral da plataforma
   - Detalhes de cada microserviço
   - Integrações

---

## 🚨 Troubleshooting Rápido

### Problema: Containers não sobem

```bash
docker-compose logs
# Verifique os erros específicos
```

### Problema: Porta em uso

```bash
# Windows
netstat -ano | findstr ":8080"

# Linux/Mac
lsof -i :8080

# Parar outros containers
docker ps
docker stop <container_id>
```

### Problema: OpenAI API Key inválida

```bash
# Verificar arquivo
cat brain-health/open_ai_api.env

# Recriar se necessário
echo "OPENAI_API_KEY=sk-your-key" > brain-health/open_ai_api.env

# Reiniciar Brain Health
docker-compose restart brain-health-app
```

### Problema: Limpar tudo e recomeçar

```bash
docker-compose down -v
docker-compose up -d --build
```

---

## 🎓 Próximos Passos

1. ✅ Siga o [DEPLOYMENT-CHECKLIST.md](DEPLOYMENT-CHECKLIST.md) para validar tudo
2. ✅ Acesse os Swagger UIs e explore as APIs
3. ✅ Execute os testes de integração
4. ✅ Configure monitoramento (se necessário)
5. ✅ Ajuste recursos se necessário (CPU, RAM)

---

## 📝 Changelog

### Versão 1.0 (13/02/2026)

**Criado:**
- ✅ docker-compose.yml unificado
- ✅ Scripts de automação (start/stop)
- ✅ Documentação completa
- ✅ .gitignore configurado
- ✅ Checklist de deployment

**Resolvido:**
- ✅ Conflito de portas entre serviços
- ✅ Kafka compartilhado entre Check Health e Brain Health
- ✅ PostgreSQL independentes para cada serviço
- ✅ Network compartilhada para comunicação

---

**Última atualização:** 13/02/2026
