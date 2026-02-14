# ✅ Checklist de Deploy - Health Tech Platform

Use este checklist para validar que toda a plataforma está funcionando corretamente.

---

## 📋 Pré-Deploy

### Requisitos
- [ ] Docker instalado e rodando
- [ ] Docker Compose instalado
- [ ] OpenAI API Key válida obtida
- [ ] Portas disponíveis: 8080, 8081, 9090, 5432, 5433, 9092, 2181, 8085, 9000, 8082

### Configuração
- [ ] Arquivo `brain-health/open_ai_api.env` criado com API key
- [ ] `.gitignore` configurado para ignorar arquivos sensíveis

---

## 🚀 Deploy

### Inicialização
- [ ] Executado `docker-compose up -d --build` ou `./start-platform.sh`
- [ ] Aguardados 3-5 minutos para inicialização completa
- [ ] Todos os 11 containers estão rodando (`docker-compose ps`)

### Containers Esperados
- [ ] `user-health-bff` - Status: UP
- [ ] `user-health-db` - Status: UP (healthy)
- [ ] `check-health-app` - Status: UP
- [ ] `check-health-db` - Status: UP (healthy)
- [ ] `brain-health-app` - Status: UP
- [ ] `kafka` - Status: UP (healthy)
- [ ] `zookeeper` - Status: UP
- [ ] `kafka-ui` - Status: UP
- [ ] `kafdrop` - Status: UP
- [ ] `kafka-rest-proxy` - Status: UP

---

## 🔍 Validação de Serviços

### User Health BFF (porta 8080)

#### Health Check
```bash
curl http://localhost:8080/actuator/health
```
- [ ] Resposta: `{"status":"UP"}`

#### Swagger UI
- [ ] Acesso: http://localhost:8080/swagger-ui.html
- [ ] Página carrega corretamente
- [ ] Endpoints visíveis na documentação

#### Criar Usuário (Teste Funcional)
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Teste User",
    "email": "teste@example.com",
    "login": "testeuser",
    "senha": "senha12345678"
  }'
```
- [ ] Resposta: Status 201 Created
- [ ] JSON retornado com dados do usuário criado

#### Login (Teste JWT)
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teste@example.com",
    "password": "senha12345678"
  }'
```
- [ ] Resposta: Status 200 OK
- [ ] Token JWT retornado
- [ ] Campo `accessToken` presente
- [ ] Campo `expiresIn` presente

#### Listar Usuários (Teste Autenticação)
```bash
# Substituir SEU_TOKEN pelo token obtido no login
curl -H "Authorization: Bearer SEU_TOKEN" http://localhost:8080/api/users
```
- [ ] Resposta: Status 200 OK
- [ ] Array com usuários retornado

---

### Check Health (porta 8081)

#### Health Check
```bash
curl http://localhost:8081/actuator/health
```
- [ ] Resposta: `{"status":"UP"}`

#### Swagger UI
- [ ] Acesso: http://localhost:8081/swagger-ui/index.html
- [ ] Página carrega corretamente
- [ ] Endpoints visíveis na documentação

#### Criar Meta (Teste Funcional)
```bash
curl -X POST http://localhost:8081/goals \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "1",
    "title": "Caminhar 30 minutos",
    "description": "Meta de exercício diário",
    "category": "SAUDE_FISICA",
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
      "badge": "walker"
    },
    "status": "active",
    "notifications": true
  }'
```
- [ ] Resposta: Status 201 Created
- [ ] JSON retornado com dados da meta criada
- [ ] Campo `goal_id` presente

#### Listar Metas
```bash
curl http://localhost:8081/goals
```
- [ ] Resposta: Status 200 OK
- [ ] Array com metas retornado

#### Verificar Publicação Kafka
- [ ] Log do check-health-app mostra mensagem publicada no Kafka
```bash
docker-compose logs check-health-app | grep "Published"
```

---

### Brain Health (porta 9090)

#### Health Check
```bash
curl http://localhost:9090/actuator/health
```
- [ ] Resposta: `{"status":"UP"}`

#### Swagger UI
- [ ] Acesso: http://localhost:9090/swagger-ui.html
- [ ] Página carrega corretamente
- [ ] Endpoints visíveis na documentação

#### Gerar Conteúdo IA (Teste Funcional)
```bash
curl -X POST http://localhost:9090/api/v1/ai/articles/search \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Benefícios da caminhada para saúde cardiovascular"
  }'
```
- [ ] Resposta: Status 200 OK
- [ ] JSON retornado com artigo gerado
- [ ] Campos presentes: `title`, `introduction`, `recommendations`, `conclusion`, `quizzes`
- [ ] OpenAI API foi chamada com sucesso

#### Verificar Consumo Kafka
- [ ] Log do brain-health-app mostra mensagens consumidas do Kafka (se houver)
```bash
docker-compose logs brain-health-app | grep "Consumed"
```

---

### Kafka Cluster

#### Broker Disponível
```bash
docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```
- [ ] Lista de APIs retornada (sem erro)

#### Tópicos Criados
```bash
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```
- [ ] Tópico `goal-created` existe (ou será criado dinamicamente)
- [ ] Tópico `goal-progress-updated` existe (ou será criado dinamicamente)

---

### Kafka UIs

#### Kafka UI (porta 8085)
- [ ] Acesso: http://localhost:8085
- [ ] Interface carrega corretamente
- [ ] Cluster "health-platform" visível
- [ ] Possível visualizar tópicos
- [ ] Possível ver mensagens nos tópicos

#### Kafdrop (porta 9000)
- [ ] Acesso: http://localhost:9000
- [ ] Interface carrega corretamente
- [ ] Broker kafka:29092 conectado
- [ ] Possível visualizar tópicos

#### Kafka REST Proxy (porta 8082)
```bash
curl http://localhost:8082/topics
```
- [ ] Resposta: Lista de tópicos em JSON

---

### Bancos de Dados

#### User Health DB (porta 5432)
```bash
docker exec -it user-health-db psql -U postgres -d postgres -c "\dt"
```
- [ ] Conexão bem-sucedida
- [ ] Tabelas criadas (users, etc.)

```bash
docker exec -it user-health-db psql -U postgres -d postgres -c "SELECT COUNT(*) FROM users;"
```
- [ ] Query executada com sucesso
- [ ] Retorna número de usuários (deve ter pelo menos 1 do teste)

#### Check Health DB (porta 5433)
```bash
docker exec -it check-health-db psql -U admin -d checkhealth -c "\dt"
```
- [ ] Conexão bem-sucedida
- [ ] Tabelas criadas (goals, etc.)

```bash
docker exec -it check-health-db psql -U admin -d checkhealth -c "SELECT COUNT(*) FROM goals;"
```
- [ ] Query executada com sucesso
- [ ] Retorna número de metas (deve ter pelo menos 1 do teste)

---

## 🔗 Testes de Integração

### Fluxo Completo: User → Goal → AI Content

#### 1. Criar Usuário
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Integração",
    "email": "joao.integracao@test.com",
    "login": "joaoint",
    "senha": "senha12345678"
  }'
```
- [ ] Usuário criado com sucesso
- [ ] `id` retornado (salvar para próximos passos)

#### 2. Fazer Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao.integracao@test.com",
    "password": "senha12345678"
  }'
```
- [ ] Token JWT retornado
- [ ] Token salvo para uso posterior

#### 3. Criar Meta com Token JWT
```bash
# Substituir USER_ID e SEU_TOKEN
curl -X POST http://localhost:8081/goals \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "USER_ID",
    "title": "Melhorar sono com meditação",
    "category": "SAUDE_MENTAL",
    "type": "daily",
    "difficulty": "easy"
  }'
```
- [ ] Meta criada com sucesso
- [ ] Evento publicado no Kafka

#### 4. Verificar Evento no Kafka UI
- [ ] Acesso: http://localhost:8085
- [ ] Tópico `goal-created` tem nova mensagem
- [ ] Mensagem contém dados da meta criada

#### 5. Gerar Conteúdo sobre a Categoria da Meta
```bash
curl -X POST http://localhost:9090/api/v1/ai/articles/search \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Como a meditação melhora a qualidade do sono?"
  }'
```
- [ ] Conteúdo gerado com sucesso
- [ ] Artigo relevante para a meta criada

---

## 📊 Monitoramento

### Logs
- [ ] Logs de todos os serviços acessíveis via `docker-compose logs -f`
- [ ] Sem erros críticos nos logs
- [ ] Conexões ao banco de dados bem-sucedidas
- [ ] Kafka conectado em todos os serviços

### Recursos
```bash
docker stats
```
- [ ] Uso de CPU dentro do esperado (<80% por container)
- [ ] Uso de memória dentro do esperado (<2GB por container de aplicação)
- [ ] Sem containers em restart loop

### Volumes
```bash
docker volume ls
```
- [ ] Volume `user-health-postgres-data` criado
- [ ] Volume `check-health-postgres-data` criado
- [ ] Volume `kafka-data` criado
- [ ] Volume `zookeeper-data` criado
- [ ] Volume `zookeeper-logs` criado

---

## 🔒 Segurança

### Senhas e Chaves
- [ ] OpenAI API Key não está commitada no Git
- [ ] Arquivo `open_ai_api.env` no `.gitignore`
- [ ] Senhas de banco padrão (só para dev/teste)

### JWT
- [ ] Tokens JWT expirando corretamente
- [ ] Endpoints protegidos requerem token
- [ ] Endpoints públicos acessíveis sem token

---

## 🎯 Performance

### Tempo de Resposta
- [ ] Health checks respondem em < 1s
- [ ] APIs CRUD respondem em < 2s
- [ ] Geração de conteúdo IA responde em < 20s

### Concorrência
- [ ] Múltiplas requisições simultâneas funcionam
- [ ] Kafka processa mensagens sem atrasos significativos

---

## 🐛 Troubleshooting

### Se algo falhar:

#### Container não inicia
```bash
docker-compose logs <service-name>
# Verificar erro específico
```

#### Porta em uso
```bash
# Windows
netstat -ano | findstr ":<porta>"
# Linux/Mac
lsof -i :<porta>
```

#### Limpar e reiniciar
```bash
docker-compose down -v
docker-compose up -d --build
```

---

## ✅ Deploy Bem-Sucedido

Se todos os itens acima estão ✅, seu deploy está completo e funcional!

### Checklist Final
- [ ] 11 containers rodando e saudáveis
- [ ] Todas as APIs acessíveis e respondendo
- [ ] Swagger UIs acessíveis
- [ ] Kafka funcionando e processando mensagens
- [ ] Bancos de dados persistindo dados
- [ ] Integração entre serviços funcionando
- [ ] Logs sem erros críticos
- [ ] Recursos dentro do esperado

---

## 📝 Notas

**Data do Deploy:** _______________

**Versão:** 1.0.0

**Ambiente:** Development / Production

**Responsável:** _______________

**Observações:**
_______________________________________________________________
_______________________________________________________________
_______________________________________________________________

---

**Última atualização:** 13/02/2026
