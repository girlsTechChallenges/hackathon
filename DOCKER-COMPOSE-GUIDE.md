# 🐳 Docker Compose Unificado - Guia de Uso

Este arquivo sobe **todos os 3 projetos** da plataforma de uma vez: **user-health-bff**, **check-health** e **brain-health**.

---

## 📋 O que será iniciado?

### Aplicações
- ✅ **User Health BFF** - porta 8080
- ✅ **Check Health** - porta 8081
- ✅ **Brain Health** - porta 9090

### Bancos de Dados
- ✅ **PostgreSQL User Health** - porta 5432
- ✅ **PostgreSQL Check Health** - porta 5433

### Mensageria Kafka
- ✅ **Zookeeper** - porta 2181
- ✅ **Kafka Broker** - porta 9092
- ✅ **Kafka UI** - porta 8085
- ✅ **Kafdrop** - porta 9000
- ✅ **Kafka REST Proxy** - porta 8082

**Total: 11 containers**

---

## 🚀 Como Usar

### Pré-requisitos

1. **Docker e Docker Compose** instalados
2. **OpenAI API Key** configurada para o Brain Health

### 1. Configurar OpenAI API Key

Antes de subir os containers, crie o arquivo de configuração do Brain Health:

**Windows (PowerShell):**
```powershell
Set-Content -Path "brain-health\open_ai_api.env" -Value "OPENAI_API_KEY=sk-your-key-here"
```

**Linux/Mac:**
```bash
echo "OPENAI_API_KEY=sk-your-key-here" > brain-health/open_ai_api.env
```

### 2. Subir Toda a Plataforma

Na raiz do projeto (`C:\TCC\FIAP`), execute:

```bash
docker-compose up -d --build
```

**O que acontece:**
- ✅ Baixa as imagens Docker necessárias
- ✅ Constrói as 3 aplicações Java
- ✅ Cria os bancos de dados PostgreSQL
- ✅ Inicia o cluster Kafka
- ✅ Sobe as interfaces de gerenciamento Kafka
- ✅ Inicia as 3 aplicações Spring Boot

**Tempo estimado:** 5-10 minutos na primeira vez (dependendo da conexão)

### 3. Verificar Status

```bash
# Ver todos os containers rodando
docker-compose ps

# Ou verificar com docker ps
docker ps
```

**Você deve ver 11 containers:**
```
user-health-bff
user-health-db
check-health-app
check-health-db
brain-health-app
kafka
zookeeper
kafka-ui
kafdrop
kafka-rest-proxy
```

### 4. Verificar Logs

```bash
# Ver logs de todos os serviços
docker-compose logs -f

# Ver logs de um serviço específico
docker-compose logs -f user-health-bff
docker-compose logs -f check-health-app
docker-compose logs -f brain-health-app
```

### 5. Verificar Health Checks

```bash
# User Health BFF
curl http://localhost:8080/actuator/health

# Check Health
curl http://localhost:8081/actuator/health

# Brain Health
curl http://localhost:9090/actuator/health
```

---

## 🔌 Acessos e URLs

### Aplicações

| Serviço | URL | Porta |
|---------|-----|-------|
| **User Health BFF** | http://localhost:8080 | 8080 |
| ↳ Swagger UI | http://localhost:8080/swagger-ui.html | |
| ↳ API | http://localhost:8080/api/users | |
| ↳ Health | http://localhost:8080/actuator/health | |
| | | |
| **Check Health** | http://localhost:8081 | 8081 |
| ↳ Swagger UI | http://localhost:8081/swagger-ui/index.html | |
| ↳ API | http://localhost:8081/goals | |
| ↳ Health | http://localhost:8081/actuator/health | |
| | | |
| **Brain Health** | http://localhost:9090 | 9090 |
| ↳ Swagger UI | http://localhost:9090/swagger-ui.html | |
| ↳ API | http://localhost:9090/api/v1/ai/articles/search | |
| ↳ Health | http://localhost:9090/actuator/health | |

### Bancos de Dados

| Database | Host | Porta | User | Password | Database |
|----------|------|-------|------|----------|----------|
| **User Health DB** | localhost | 5432 | postgres | postgres | postgres |
| **Check Health DB** | localhost | 5433 | admin | admin123 | checkhealth |

### Kafka

| Serviço | URL | Porta |
|---------|-----|-------|
| **Kafka Broker** | localhost:9092 | 9092 |
| **Zookeeper** | localhost:2181 | 2181 |
| **Kafka UI** | http://localhost:8085 | 8085 |
| **Kafdrop** | http://localhost:9000 | 9000 |
| **REST Proxy** | http://localhost:8082 | 8082 |

---

## 🧪 Teste Rápido

### 1. Criar Usuário (User Health BFF)

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

### 2. Fazer Login e Obter Token

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@test.com",
    "password": "senha12345678"
  }'
```

**Salve o `accessToken` retornado!**

### 3. Criar Meta de Saúde (Check Health)

```bash
curl -X POST http://localhost:8081/goals \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "1",
    "title": "Meditar diariamente",
    "description": "Praticar meditação por 15 minutos",
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

### 4. Gerar Conteúdo com IA (Brain Health)

```bash
curl -X POST http://localhost:9090/api/v1/ai/articles/search \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Quais são os benefícios da meditação para ansiedade?"
  }'
```

---

## 🛑 Parar os Serviços

### Parar mas manter os dados

```bash
docker-compose stop
```

### Parar e remover containers (mantém volumes/dados)

```bash
docker-compose down
```

### Parar, remover containers E VOLUMES (apaga dados)

```bash
docker-compose down -v
```

**⚠️ CUIDADO:** O comando com `-v` apaga todos os dados dos bancos de dados!

---

## 🔄 Reiniciar Serviços

### Reiniciar um serviço específico

```bash
docker-compose restart user-health-bff
docker-compose restart check-health-app
docker-compose restart brain-health-app
```

### Reiniciar todos

```bash
docker-compose restart
```

### Rebuild e restart de um serviço específico

```bash
docker-compose up -d --build user-health-bff
docker-compose up -d --build check-health-app
docker-compose up -d --build brain-health-app
```

---

## 🐛 Troubleshooting

### Problema: Portas em Uso

Se você receber erro de porta já em uso:

```bash
# Verificar o que está usando as portas
# Windows (PowerShell)
netstat -ano | findstr ":8080"
netstat -ano | findstr ":8081"
netstat -ano | findstr ":9090"

# Linux/Mac
lsof -i :8080
lsof -i :8081
lsof -i :9090
```

**Solução:** Pare outros containers que possam estar usando as portas:
```bash
docker ps
docker stop <container_id>
```

### Problema: OpenAI API Key Não Configurada

Se o Brain Health não iniciar, verifique se o arquivo existe:

```bash
# Windows
Get-Content brain-health\open_ai_api.env

# Linux/Mac
cat brain-health/open_ai_api.env
```

Se não existir, crie conforme instruções na seção [1. Configurar OpenAI API Key](#1-configurar-openai-api-key).

### Problema: Container em CrashLoop

Verifique os logs do container com problema:

```bash
docker-compose logs <service_name>

# Exemplos:
docker-compose logs user-health-bff
docker-compose logs check-health-app
docker-compose logs brain-health-app
```

**Causas comuns:**
- Banco de dados ainda não está pronto → Aguarde o healthcheck
- Kafka ainda não está pronto → Aguarde alguns segundos
- Erro na aplicação → Verifique os logs

### Problema: Aplicação Não Conecta no Banco

Verifique se o banco está rodando e saudável:

```bash
# Ver status dos containers
docker-compose ps

# Testar conexão com o banco manualmente
docker exec -it user-health-db psql -U postgres -d postgres -c "\dt"
docker exec -it check-health-db psql -U admin -d checkhealth -c "\dt"
```

### Problema: Kafka Não Está Pronto

Aguarde alguns segundos após o `docker-compose up`. O Kafka pode levar 30-60 segundos para ficar pronto.

Verifique:
```bash
docker-compose logs kafka | grep "started"
```

### Limpar Tudo e Recomeçar

Se tudo der errado, você pode limpar completamente e recomeçar:

```bash
# Parar e remover tudo (containers, networks, volumes)
docker-compose down -v

# Remover imagens das aplicações
docker rmi user-health-bff-user-health-bff
docker rmi check-health-check-health-app  
docker rmi brain-health-brain-health-app

# Limpar cache do Docker (opcional)
docker system prune -a

# Subir novamente do zero
docker-compose up -d --build
```

---

## 📊 Monitoramento

### Ver Recursos Consumidos

```bash
docker stats
```

### Ver Uso de Volumes

```bash
docker volume ls
docker volume inspect <volume_name>
```

### Ver Uso de Rede

```bash
docker network ls
docker network inspect health-platform-network
```

---

## 🔧 Configurações Avançadas

### Executar em Background + Follow Logs

```bash
# Subir em background
docker-compose up -d

# Seguir logs de serviços específicos
docker-compose logs -f user-health-bff check-health-app brain-health-app
```

### Escalar Serviços (Não Aplicável para DBs)

```bash
# Caso queira múltiplas instâncias (apenas para apps sem DB)
docker-compose up -d --scale brain-health-app=2
```

**⚠️ Nota:** Isso não funciona bem com as configurações atuais devido às portas fixas.

### Limitar Recursos

Edite o `docker-compose.yml` e adicione:

```yaml
services:
  user-health-bff:
    # ... outras configs
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2G
        reservations:
          memory: 512M
```

---

## 📝 Ordem de Inicialização

O Docker Compose garante a seguinte ordem:

1. **Zookeeper** - Base do Kafka
2. **Kafka** - Depende do Zookeeper
3. **Bancos de Dados** (user-health-db, check-health-db) - Paralelo
4. **User Health BFF** - Depende de user-health-db
5. **Check Health** - Depende de check-health-db e Kafka
6. **Brain Health** - Depende de Kafka
7. **Kafka UIs** - Dependem de Kafka

**Tempo total de inicialização:** ~3-5 minutos para tudo ficar ready.

---

## 🎯 Próximos Passos

Após subir a plataforma:

1. ✅ Acesse os Swagger UIs para explorar as APIs
2. ✅ Crie usuários no User Health BFF
3. ✅ Faça login e obtenha o token JWT
4. ✅ Crie metas de saúde no Check Health
5. ✅ Gere conteúdo médico no Brain Health
6. ✅ Monitore eventos Kafka nas interfaces
7. ✅ Verifique os bancos de dados

---

## 📚 Documentação Adicional

- [README.md](README.md) - Documentação completa da plataforma
- [user-health-bff/README.md](user-health-bff/README.md) - Docs do User Health BFF
- [check-health/README.md](check-health/README.md) - Docs do Check Health
- [brain-health/README.md](brain-health/README.md) - Docs do Brain Health

---

**Última atualização:** 13/02/2026
