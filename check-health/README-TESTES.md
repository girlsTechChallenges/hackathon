# 🧪 Testes de Integração - Check Health API

Este documento explica os testes de integração criados para a API de metas de saúde gamificada, seguindo os princípios de TDD.

## 📋 Estrutura dos Testes

### 1. GoalControllerIntegrationTest
**Arquivo**: `GoalControllerIntegrationTest.java`

**Objetivo**: Testa a integração completa da API REST (Controller → Service → Repository)

**Cenários testados**:
- ✅ **Criação de Metas**: Validação de dados, campos obrigatórios, persistência
- ✅ **Listagem de Metas**: Lista vazia, múltiplas metas, propriedades corretas
- ✅ **Busca por ID**: Meta existente, meta inexistente, ID inválido
- ✅ **Atualização de Metas**: Dados válidos, meta inexistente, validações
- ✅ **Remoção de Metas**: Meta existente, meta inexistente
- ✅ **Atualização de Progresso**: Incrementos válidos, conclusão automática, validações
- ✅ **Gamificação**: Cálculo de pontos e badges

**Lógica dos testes**:
- Usa MockMvc para simular requisições HTTP
- Valida códigos de status HTTP (200, 201, 404, 400)
- Verifica estrutura JSON da resposta
- Confirma persistência no banco de dados H2 em memória
- Testa casos de sucesso E falha

### 2. GoalRepositoryServiceIntegrationTest
**Arquivo**: `GoalRepositoryServiceIntegrationTest.java`

**Objetivo**: Testa consultas específicas e regras de negócio na persistência

**Cenários testados**:
- 🔍 **Consultas por Categoria**: Filtros por SAUDE_FISICA, NUTRICAO, etc.
- 👤 **Consultas por Usuário**: Metas específicas por usuário
- 📊 **Consultas por Status**: Metas ativas, completadas, arquivadas
- 📅 **Consultas por Período**: Filtros de data de início/fim
- 🔒 **Integridade de Dados**: Múltiplas inserções, timestamps automáticos
- 📜 **Regras de Negócio**: Múltiplas metas ativas, categorias por usuário

**Lógica dos testes**:
- Usa @Transactional para isolamento entre testes
- Testa métodos personalizados do repository
- Valida comportamento de JPA e Spring Data
- Verifica regras de negócio específicas do domínio

### 3. GoalEndToEndKafkaIntegrationTest
**Arquivo**: `GoalEndToEndKafkaIntegrationTest.java`

**Objetivo**: Testa fluxo completo com eventos Kafka (API → Banco → Eventos)

**Cenários testados**:
- 📢 **Eventos de Criação**: GoalCreated publicado após criação bem-sucedida
- 📈 **Eventos de Progresso**: ProgressUpdated a cada atualização
- 🏆 **Eventos de Conclusão**: GoalCompleted quando meta é finalizada
- 🔄 **Consistência Transacional**: Rollback não publica eventos
- ❌ **Tratamento de Falhas**: Erros não geram eventos órfãos

**Lógica dos testes**:
- Usa EmbeddedKafka para simular broker em memória
- Consome eventos em tempo real durante testes
- Valida estrutura e conteúdo dos eventos JSON
- Testa consistência entre estado do banco e eventos publicados

## 🚀 Como Executar os Testes

### Executar Todos os Testes de Integração
```bash
mvn test -Dtest="**/*IntegrationTest"
```

### Executar Teste Específico
```bash
# Teste de Controller
mvn test -Dtest="GoalControllerIntegrationTest"

# Teste de Repository/Service  
mvn test -Dtest="GoalRepositoryServiceIntegrationTest"

# Teste End-to-End com Kafka
mvn test -Dtest="GoalEndToEndKafkaIntegrationTest"
```

### Executar com Relatório de Cobertura
```bash
mvn clean test jacoco:report
```
Relatório gerado em: `target/site/jacoco/index.html`

## ⚙️ Configuração dos Testes

### Perfil de Teste (`application-test.yml`)
- **Banco**: H2 em memória (isolado entre execuções)
- **Kafka**: EmbeddedKafka (não requer broker externo)
- **Logs**: DEBUG habilitado para análise
- **Docker**: Desabilitado para acelerar execução

### Dependências de Teste
- **Spring Boot Test**: Framework de teste integrado
- **Testcontainers**: Containers para integração (opcional)
- **EmbeddedKafka**: Broker Kafka em memória
- **AssertJ**: Assertions mais legíveis
- **MockMvc**: Simulação de requisições HTTP

## 🧭 Princípios TDD Aplicados

### 1. Red-Green-Refactor
- **Red**: Escrever teste que falha
- **Green**: Implementar código mínimo para passar
- **Refactor**: Melhorar sem quebrar testes

### 2. Testes Claros e Organizados
- **@DisplayName**: Descrições em português explicativas
- **@Nested**: Agrupamento lógico de cenários
- **Given-When-Then**: Estrutura AAA nos testes

### 3. Isolamento e Independência
- **@Transactional**: Rollback automático entre testes
- **@DirtiesContext**: Limpa contexto quando necessário
- **setUp()**: Preparação consistente para cada teste

## 📊 Cobertura Esperada

### Funcionalidades Cobertas
- ✅ CRUD completo de metas
- ✅ Sistema de progresso e gamificação
- ✅ Publicação de eventos Kafka
- ✅ Validações de entrada e negócio
- ✅ Tratamento de erros e casos extremos

### Cenários de Falha Testados
- ❌ Dados inválidos/incompletos
- ❌ Recursos não encontrados (404)
- ❌ Violações de regras de negócio
- ❌ Falhas na publicação de eventos

## 🔧 Resolução de Problemas

### Erro: H2 Database Lock
```bash
# Adicionar ao application-test.yml
spring.datasource.url: jdbc:h2:mem:testdb;DB_CLOSE_ON_EXIT=FALSE
```

### Erro: Kafka Timeout
```bash
# Aumentar timeout nos testes
records.poll(15, TimeUnit.SECONDS);  # ao invés de 10
```

### Erro: Port Already in Use
```bash
# Usar porta aleatória
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
```

## 📚 Próximos Passos

1. **Testes de Performance**: JMeter ou Gatling para carga
2. **Testes de Contrato**: Pact para contratos entre serviços
3. **Testes de Segurança**: Validação de autenticação/autorização
4. **Testes de Resiliência**: Circuit breaker e retry policies
5. **Testes Mutation**: PITest para qualidade dos testes

---

> 💡 **Dica**: Execute os testes regularmente durante desenvolvimento para garantir que novas funcionalidades não quebrem comportamentos existentes!
      
> 🎯 **Meta**: Manter cobertura acima de 80% e tempo de execução abaixo de 2 minutos para feedback rápido.