# Configuração Kafka - Check Health

Este documento descreve como utilizar o Apache Kafka integrado no projeto Check Health para publicação de eventos.

## Recursos Implementados

### GoalEventPublisher
Publisher responsável por enviar eventos quando um novo goal é criado.

**Tópico**: `goal.created`

**Payload do Evento**:
```json
{
  "goalId": 123,
  "userId": "user123",
  "category": "SAUDE_FISICA",
  "title": "Run 5km",
  "description": "Goal de correr 5km toda semana"
}
```

## Como Usar

### 1. Subir o Ambiente

```bash
# Subir Kafka, Zookeeper e PostgreSQL
docker compose up -d

# Verificar se os serviços estão funcionando
docker compose ps
```

### 2. Subir a Aplicação

```bash
# Compilar e executar a aplicação
./mvnw spring-boot:run
```

### 3. Testar o Publisher

Crie um goal usando a API e verifique os logs da aplicação:

```bash
# POST /api/goals
curl -X POST http://localhost:8080/api/goals \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "user123",
    "title": "Run 5km",
    "description": "Goal de correr 5km toda semana",
    "category": "SAUDE_FISICA",
    "type": "weekly",
    "start_date": "2026-02-08",
    "difficulty": "medium"
  }'
```

### 4. Monitorar Eventos (Opcional)

Para visualizar os eventos no Kafka UI:

```bash
# Subir também o Kafka UI
docker compose --profile tools up -d

# Acessar: http://localhost:9090
```

## Configurações

### Application.yml
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      client-id: check-health-producer
      acks: all
      retries: 3
```

### Docker Compose
- **Zookeeper**: `localhost:2181`
- **Kafka**: `localhost:9092`
- **Kafka UI**: `localhost:9090` (perfil tools)

## Estrutura dos Componentes

```
src/main/java/com/fiap/check/health/
├── dto/event/
│   └── GoalCreatedEvent.java          # DTO do evento
├── event/publisher/
│   └── GoalEventPublisher.java       # Publisher Kafka
├── config/
│   └── KafkaConfig.java              # Configuração do Kafka
└── service/impl/
    └── GoalServiceImpl.java          # Integração do publisher
```

## Logs

O publisher gera logs informativos:

```
INFO  - Enviando evento goal.created para o tópico goal.created - goalId: 1, userId: user123
INFO  - Evento goal.created enviado com sucesso - offset: 0, partition: 0
```

## Troubleshooting

### Kafka não conecta
- Verifique se o Docker Compose está rodando
- Confirme que a porta 9092 não está em uso

### Eventos não são enviados
- Verifique os logs da aplicação
- Confirme se o tópico foi criado automaticamente
- Use o Kafka UI para monitorar os tópicos

### Performance
- O publisher é assíncrono e não bloqueia a criação de goals
- Erros no envio de eventos são logados mas não afetam a operação principal

## Próximos Passos

Para implementar consumers (consumidores) dos eventos:

1. Criar classes `@KafkaListener` anotadas
2. Configurar consumer groups no `application.yml`
3. Implementar handlers específicos para cada tipo de evento