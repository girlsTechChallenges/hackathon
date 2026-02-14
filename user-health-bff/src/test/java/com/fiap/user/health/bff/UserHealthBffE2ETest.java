package com.fiap.user.health.bff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.user.health.bff.dto.request.UserAuthRequestDto;
import com.fiap.user.health.bff.dto.request.UserCredentialsRequestDto;
import com.fiap.user.health.bff.dto.request.UserRequestDto;
import com.fiap.user.health.bff.dto.response.UserResponseDto;
import com.fiap.user.health.bff.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("End-to-End Tests - Fluxos Completos como Usuário Externo")
class UserHealthBffE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser
    @DisplayName("E2E: Fluxo completo CRUD - Criar → Buscar → Atualizar → Deletar usuário")
    void shouldCompleteFullUserCRUDLifecycle() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("\n📋 Iniciando teste E2E: Fluxo completo CRUD de usuário");

        System.out.println("\n1️⃣ Criando novo usuário...");
        UserRequestDto createRequest = new UserRequestDto(
                "Carlos Eduardo Silva",
                "carlos.eduardo@email.com",
                "carloseduardo",
                "SenhaSegura@123"
        );

        MvcResult createResult = mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Carlos Eduardo Silva"))
                .andExpect(jsonPath("$.email").value("carlos.eduardo@email.com"))
                .andExpect(jsonPath("$.login").value("carloseduardo"))
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        UserResponseDto createdUser = objectMapper.readValue(createResponse, UserResponseDto.class);
        Long userId = createdUser.id();
        System.out.println("✅ Usuário criado com ID: " + userId);

        assertThat(userRepository.findById(userId)).isPresent();

        System.out.println("\n2️⃣ Buscando usuário por ID: " + userId);
        mockMvc.perform(get("/api/users/" + userId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.nome").value("Carlos Eduardo Silva"))
                .andExpect(jsonPath("$.email").value("carlos.eduardo@email.com"));
        System.out.println("✅ Usuário encontrado com sucesso");

        System.out.println("\n3️⃣ Listando todos os usuários...");
        mockMvc.perform(get("/api/users")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Carlos Eduardo Silva"));
        System.out.println("✅ Lista retornada com 1 usuário");

        System.out.println("\n4️⃣ Atualizando dados do usuário...");
        String updateJson = """
                {
                    "email": "carlos.novo@email.com",
                    "login": "carlosnovo",
                    "senha": "NovaSenhaSegura@456"
                }
                """;

        mockMvc.perform(put("/api/users/" + userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("carlos.novo@email.com"))
                .andExpect(jsonPath("$.login").value("carlosnovo"));
        System.out.println("✅ Usuário atualizado com sucesso");

        assertThat(userRepository.findById(userId))
                .isPresent()
                .get()
                .satisfies(user -> {
                    assertThat(user.getEmail()).isEqualTo("carlos.novo@email.com");
                    assertThat(user.getLogin()).isEqualTo("carlosnovo");
                });

        System.out.println("\n5️⃣ Deletando usuário...");
        mockMvc.perform(delete("/api/users/" + userId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
        System.out.println("✅ Usuário deletado com sucesso");

        assertThat(userRepository.findById(userId)).isEmpty();

        System.out.println("\n6️⃣ Verificando que usuário não existe mais...");
        mockMvc.perform(get("/api/users/" + userId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
        System.out.println("✅ Confirmado: usuário não existe mais");

        System.out.println("\n🎉 Teste E2E CRUD completo finalizado com sucesso!\n");
    }

    @Test
    @WithMockUser
    @DisplayName("E2E: Deve impedir cadastro de usuário com email duplicado")
    void shouldPreventDuplicateEmail() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        UserRequestDto firstUser = new UserRequestDto(
                "Primeiro Usuário",
                "duplicado@email.com",
                "primeirousuario",
                "senha123456"
        );

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isCreated());

        UserRequestDto secondUser = new UserRequestDto(
                "Segundo Usuário",
                "duplicado@email.com",
                "segundousuario",
                "senha789012"
        );

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isConflict());

        long count = userRepository.count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @WithMockUser
    @DisplayName("E2E: Deve listar múltiplos usuários")
    void shouldListMultipleUsers() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        UserRequestDto user1 = new UserRequestDto("Ana Silva", "ana@email.com", "anasilva123", "senha12345678");
        UserRequestDto user2 = new UserRequestDto("Bruno Costa", "bruno@email.com", "brunocosta1", "senha87654321");
        UserRequestDto user3 = new UserRequestDto("Carla Dias", "carla@email.com", "carladias12", "senha11223344");

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user3)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].nome", containsInAnyOrder("Ana Silva", "Bruno Costa", "Carla Dias")));

        assertThat(userRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("E2E: Deve realizar login com credenciais válidas")
    void shouldLoginWithValidCredentials() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        // 1. Criar usuário primeiro
        UserRequestDto createRequest = new UserRequestDto(
                "Login User",
                "login@email.com",
                "loginuser",
                "senhaParaLogin123"
        );

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        // 2. Realizar login
        UserCredentialsRequestDto loginRequest = new UserCredentialsRequestDto(
                "login@email.com",
                "senhaParaLogin123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    @DisplayName("E2E: Não deve realizar login com credenciais inválidas")
    void shouldNotLoginWithInvalidCredentials() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        // 1. Criar usuário
        UserRequestDto createRequest = new UserRequestDto(
                "Test User",
                "test@email.com",
                "testuser",
                "senhaCorreta123"
        );

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        // 2. Tentar login com senha errada
        UserCredentialsRequestDto wrongPasswordLogin = new UserCredentialsRequestDto(
                "test@email.com",
                "senhaErrada123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPasswordLogin)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("E2E: Deve validar campos obrigatórios no cadastro")
    void shouldValidateRequiredFieldsOnCreate() throws Exception {
        String invalidJson1 = """
                {
                    "nome": "",
                    "email": "valido@email.com",
                    "login": "validologin",
                    "senha": "senhaValida123"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson1))
                .andExpect(status().isBadRequest());

        String invalidJson2 = """
                {
                    "nome": "Nome Válido",
                    "email": "email-sem-arroba",
                    "login": "validologin",
                    "senha": "senhaValida123"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson2))
                .andExpect(status().isBadRequest());

        String invalidJson3 = """
                {
                    "nome": "Nome Válido",
                    "email": "valido@email.com",
                    "login": "validologin",
                    "senha": "123"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson3))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    @WithMockUser
    @DisplayName("E2E: Deve atualizar senha do usuário através do endpoint de auth")
    void shouldUpdatePasswordThroughAuthEndpoint() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        // 1. Criar usuário
        UserRequestDto createRequest = new UserRequestDto(
                "Update Password User",
                "updatepwd@email.com",
                "updatepwd",
                "senhaAntiga123"
        );

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        // 2. Atualizar senha
        UserCredentialsRequestDto updatePassword = new UserCredentialsRequestDto(
                "updatepwd@email.com",
                "senhaNova456"
        );

        mockMvc.perform(patch("/api/v1/auth/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePassword)))
                .andExpect(status().isNoContent());

        // 3. Verificar que não consegue fazer login com senha antiga
        UserCredentialsRequestDto oldPasswordLogin = new UserCredentialsRequestDto(
                "updatepwd@email.com",
                "senhaAntiga123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oldPasswordLogin)))
                .andExpect(status().isUnauthorized());

        // 4. Verificar que consegue fazer login com senha nova
        UserCredentialsRequestDto newPasswordLogin = new UserCredentialsRequestDto(
                "updatepwd@email.com",
                "senhaNova456"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPasswordLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    // ==================== FLUXO COMPLETO DE LOGIN E AUTENTICAÇÃO ====================

    @Test
    @DisplayName("E2E: Fluxo completo - Cadastrar → Login → Validar JWT")
    void shouldCompleteRegistrationAndLoginFlow() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("\n🔐 Iniciando teste E2E: Fluxo de Cadastro e Login");

        // ===== 1. CADASTRAR USUÁRIO =====
        System.out.println("\n1️⃣ Cadastrando novo usuário...");
        UserRequestDto registerRequest = new UserRequestDto(
                "Fernanda Santos",
                "fernanda.santos@email.com",
                "fernandasantos",
                "MinhaSenhaSegura@2024"
        );

        MvcResult registerResult = mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Fernanda Santos"))
                .andReturn();

        String registerResponse = registerResult.getResponse().getContentAsString();
        UserResponseDto registeredUser = objectMapper.readValue(registerResponse, UserResponseDto.class);
        System.out.println("✅ Usuário cadastrado: " + registeredUser.email());

        // ===== 2. REALIZAR LOGIN =====
        System.out.println("\n2️⃣ Realizando login...");
        UserCredentialsRequestDto loginRequest = new UserCredentialsRequestDto(
                "fernanda.santos@email.com",
                "MinhaSenhaSegura@2024"
        );

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andReturn();

        // ===== 3. VALIDAR ESTRUTURA DO JWT =====
        System.out.println("\n3️⃣ Validando estrutura do JWT...");
        String loginResponse = loginResult.getResponse().getContentAsString();
        UserAuthRequestDto authResponse = objectMapper.readValue(loginResponse, UserAuthRequestDto.class);

        String jwt = authResponse.accessToken();
        assertThat(jwt).isNotNull().isNotEmpty();

        // JWT deve ter 3 partes: header.payload.signature
        String[] jwtParts = jwt.split("\\.");
        assertThat(jwtParts).hasSize(3);
        System.out.println("✅ JWT válido com 3 partes: Header, Payload, Signature");
        System.out.println("✅ Tempo de expiração: " + authResponse.expiresIn() + " segundos");

        System.out.println("\n🎉 Teste E2E de Login completo finalizado com sucesso!\n");
    }

    // ==================== FLUXO DE ATUALIZAÇÃO ====================

    @Test
    @WithMockUser
    @DisplayName("E2E: Fluxo completo - Criar → Atualizar dados → Atualizar senha")
    void shouldCompleteUpdateFlow() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("\n📝 Iniciando teste E2E: Fluxo de Atualização de Usuário");

        // ===== 1. CRIAR USUÁRIO =====
        System.out.println("\n1️⃣ Criando usuário inicial...");
        UserRequestDto createRequest = new UserRequestDto(
                "Roberto Oliveira",
                "roberto@email.com",
                "robertooliveira",
                "SenhaOriginal@123"
        );

        MvcResult createResult = mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        UserResponseDto createdUser = objectMapper.readValue(createResponse, UserResponseDto.class);
        Long userId = createdUser.id();
        System.out.println("✅ Usuário criado com ID: " + userId);

        // ===== 2. ATUALIZAR DADOS DO USUÁRIO =====
        System.out.println("\n2️⃣ Atualizando email e login...");
        String updateDataJson = """
                {
                    "email": "roberto.novo@email.com",
                    "login": "robertonovo",
                    "senha": "SenhaOriginal@123"
                }
                """;

        mockMvc.perform(put("/api/users/" + userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateDataJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("roberto.novo@email.com"))
                .andExpect(jsonPath("$.login").value("robertonovo"));
        System.out.println("✅ Dados atualizados com sucesso");

        // ===== 3. ATUALIZAR SENHA =====
        System.out.println("\n3️⃣ Atualizando senha...");
        UserCredentialsRequestDto updatePasswordRequest = new UserCredentialsRequestDto(
                "roberto.novo@email.com",
                "NovaSenhaSegura@456"
        );

        mockMvc.perform(patch("/api/v1/auth/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePasswordRequest)))
                .andDo(print())
                .andExpect(status().isNoContent());
        System.out.println("✅ Senha atualizada com sucesso");

        // ===== 4. VALIDAR LOGIN COM SENHA NOVA =====
        System.out.println("\n4️⃣ Validando login com senha nova...");
        UserCredentialsRequestDto newLoginRequest = new UserCredentialsRequestDto(
                "roberto.novo@email.com",
                "NovaSenhaSegura@456"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLoginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
        System.out.println("✅ Login com senha nova bem-sucedido");

        // ===== 5. VERIFICAR QUE SENHA ANTIGA NÃO FUNCIONA =====
        System.out.println("\n5️⃣ Verificando que senha antiga não funciona mais...");
        UserCredentialsRequestDto oldLoginRequest = new UserCredentialsRequestDto(
                "roberto.novo@email.com",
                "SenhaOriginal@123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oldLoginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        System.out.println("✅ Confirmado: senha antiga não funciona mais");

        System.out.println("\n🎉 Teste E2E de Atualização completo finalizado com sucesso!\n");
    }

    // ==================== FLUXOS DE LISTAGEM ====================

    @Test
    @WithMockUser
    @DisplayName("E2E: Fluxo de listagem - Lista vazia → Criar múltiplos → Listar todos → Deletar → Lista vazia")
    void shouldCompleteListingFlow() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("\n📋 Iniciando teste E2E: Fluxo de Listagem");

        // ===== 1. VERIFICAR LISTA VAZIA =====
        System.out.println("\n1️⃣ Verificando lista vazia inicialmente...");
        mockMvc.perform(get("/api/users")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        System.out.println("✅ Lista vazia confirmada");

        // ===== 2. CRIAR MÚLTIPLOS USUÁRIOS =====
        System.out.println("\n2️⃣ Criando múltiplos usuários...");
        String[] nomes = {"Alice Costa", "Bruno Silva", "Carla Pereira", "Daniel Santos", "Elena Rodrigues"};
        String[] emails = {"alice@email.com", "bruno@email.com", "carla@email.com", "daniel@email.com", "elena@email.com"};
        String[] logins = {"alicecosta", "brunosilva", "carlapereira", "danielsantos", "elenarodrigues"};

        for (int i = 0; i < nomes.length; i++) {
            UserRequestDto user = new UserRequestDto(nomes[i], emails[i], logins[i], "Senha@123" + i);
            mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user)))
                    .andExpect(status().isCreated());
            System.out.println("  ✓ Usuário " + (i + 1) + " criado: " + nomes[i]);
        }
        System.out.println("✅ Todos os 5 usuários criados");

        // ===== 3. LISTAR TODOS OS USUÁRIOS =====
        System.out.println("\n3️⃣ Listando todos os usuários...");
        MvcResult listResult = mockMvc.perform(get("/api/users")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].nome", containsInAnyOrder(nomes)))
                .andReturn();

        System.out.println("✅ Lista retornada com 5 usuários");

        // ===== 4. VERIFICAR DETALHES DE CADA USUÁRIO =====
        System.out.println("\n4️⃣ Verificando detalhes de cada usuário...");
        String listResponse = listResult.getResponse().getContentAsString();
        UserResponseDto[] users = objectMapper.readValue(listResponse, UserResponseDto[].class);

        for (UserResponseDto user : users) {
            mockMvc.perform(get("/api/users/" + user.id())
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(user.id()))
                    .andExpect(jsonPath("$.nome").value(user.nome()));
            System.out.println("  ✓ Usuário verificado: " + user.nome());
        }
        System.out.println("✅ Todos os usuários verificados individualmente");

        // ===== 5. DELETAR TODOS OS USUÁRIOS =====
        System.out.println("\n5️⃣ Deletando todos os usuários...");
        for (UserResponseDto user : users) {
            mockMvc.perform(delete("/api/users/" + user.id())
                            .with(csrf()))
                    .andExpect(status().isNoContent());
            System.out.println("  ✓ Usuário deletado: " + user.nome());
        }
        System.out.println("✅ Todos os usuários deletados");

        // ===== 6. VERIFICAR LISTA VAZIA NOVAMENTE =====
        System.out.println("\n6️⃣ Verificando lista vazia após deleções...");
        mockMvc.perform(get("/api/users")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        System.out.println("✅ Lista vazia confirmada");

        // Verificar no banco
        assertThat(userRepository.count()).isEqualTo(0);

        System.out.println("\n🎉 Teste E2E de Listagem completo finalizado com sucesso!\n");
    }

    // ==================== FLUXOS DE VALIDAÇÃO E REGRAS DE NEGÓCIO ====================

    @Test
    @WithMockUser
    @DisplayName("E2E: Fluxo de validações - Testar todas as regras de negócio")
    void shouldValidateAllBusinessRules() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("\n✅ Iniciando teste E2E: Validações e Regras de Negócio");

        // ===== 1. VALIDAR EMAIL DUPLICADO =====
        System.out.println("\n1️⃣ Testando rejeição de email duplicado...");
        UserRequestDto firstUser = new UserRequestDto(
                "Primeiro Usuário",
                "duplicado@email.com",
                "primeirousuario",
                "Senha@123456"
        );

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isCreated());
        System.out.println("  ✓ Primeiro usuário criado");

        UserRequestDto duplicateUser = new UserRequestDto(
                "Segundo Usuário",
                "duplicado@email.com",
                "segundousuario",
                "Senha@789012"
        );

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isConflict());
        System.out.println("✅ Email duplicado corretamente rejeitado (409 Conflict)");

        // ===== 2. VALIDAR FORMATO DE EMAIL =====
        System.out.println("\n2️⃣ Testando validação de formato de email...");
        String invalidEmailJson = """
                {
                    "nome": "Usuário Email Inválido",
                    "email": "email-sem-arroba-nem-dominio",
                    "login": "emailinvalido",
                    "senha": "Senha@12345678"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEmailJson))
                .andExpect(status().isBadRequest());
        System.out.println("✅ Email inválido corretamente rejeitado (400 Bad Request)");

        // ===== 3. VALIDAR TAMANHO MÍNIMO DE SENHA =====
        System.out.println("\n3️⃣ Testando validação de tamanho mínimo de senha...");
        String shortPasswordJson = """
                {
                    "nome": "Usuário Senha Curta",
                    "email": "senhacurta@email.com",
                    "login": "senhacurta",
                    "senha": "123"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shortPasswordJson))
                .andExpect(status().isBadRequest());
        System.out.println("✅ Senha curta corretamente rejeitada (400 Bad Request)");

        // ===== 4. VALIDAR NOME COM CARACTERES ESPECIAIS =====
        System.out.println("\n4️⃣ Testando validação de nome (só letras)...");
        String invalidNameJson = """
                {
                    "nome": "Nome123ComNumeros",
                    "email": "nomeinvalido@email.com",
                    "login": "nomeinvalido",
                    "senha": "Senha@12345678"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidNameJson))
                .andExpect(status().isBadRequest());
        System.out.println("✅ Nome inválido corretamente rejeitado (400 Bad Request)");

        // ===== 5. VALIDAR BUSCA DE USUÁRIO INEXISTENTE =====
        System.out.println("\n5️⃣ Testando busca de usuário inexistente...");
        mockMvc.perform(get("/api/users/99999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
        System.out.println("✅ Usuário inexistente retorna 404 Not Found");

        // ===== 6. VALIDAR ATUALIZAÇÃO DE USUÁRIO INEXISTENTE =====
        System.out.println("\n6️⃣ Testando atualização de usuário inexistente...");
        String updateJson = """
                {
                    "email": "novo@email.com",
                    "login": "novologin",
                    "senha": "NovaSenha@123"
                }
                """;

        mockMvc.perform(put("/api/users/99999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
        System.out.println("✅ Atualização de usuário inexistente retorna 404 Not Found");

        System.out.println("\n🎉 Teste E2E de Validações completo finalizado com sucesso!\n");
    }

    // ==================== FLUXO REALISTA DE USUÁRIO COMPLETO ====================

    @Test
    @DisplayName("E2E: Fluxo realista completo - Simula jornada completa de um usuário")
    void shouldCompleteRealisticUserJourney() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("\n🎭 Iniciando teste E2E: Jornada Realista Completa de Usuário");
        System.out.println("Simulando: Registro → Login → Atualização de Perfil → Mudança de Senha → Logout → Novo Login");

        // ===== ETAPA 1: REGISTRO =====
        System.out.println("\n📝 ETAPA 1: Novo usuário se registra na plataforma");
        UserRequestDto registrationData = new UserRequestDto(
                "Marina Oliveira",
                "marina.oliveira@healthapp.com",
                "marinaoliveira",
                "MinhaPrimeiraSenha@2024"
        );

        MvcResult registrationResult = mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationData)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Marina Oliveira"))
                .andExpect(jsonPath("$.email").value("marina.oliveira@healthapp.com"))
                .andReturn();

        String registrationResponse = registrationResult.getResponse().getContentAsString();
        UserResponseDto registeredUser = objectMapper.readValue(registrationResponse, UserResponseDto.class);
        Long userId = registeredUser.id();
        System.out.println("✅ Usuário registrado com sucesso! ID: " + userId);

        // ===== ETAPA 2: PRIMEIRO LOGIN =====
        System.out.println("\n🔐 ETAPA 2: Usuário faz login pela primeira vez");
        UserCredentialsRequestDto firstLoginCredentials = new UserCredentialsRequestDto(
                "marina.oliveira@healthapp.com",
                "MinhaPrimeiraSenha@2024"
        );

        MvcResult firstLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstLoginCredentials)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andReturn();

        String firstLoginResponse = firstLoginResult.getResponse().getContentAsString();
        UserAuthRequestDto firstAuthResponse = objectMapper.readValue(firstLoginResponse, UserAuthRequestDto.class);
        String jwtToken = firstAuthResponse.accessToken();
        System.out.println("✅ Login bem-sucedido! Token JWT gerado (expira em " + firstAuthResponse.expiresIn() + " segundos)");

        // ===== ETAPA 3: USUÁRIO CONSULTA SEU PERFIL =====
        System.out.println("\n👤 ETAPA 3: Usuário consulta seu perfil");
        mockMvc.perform(get("/api/users/" + userId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Marina Oliveira"))
                .andExpect(jsonPath("$.email").value("marina.oliveira@healthapp.com"));
        System.out.println("✅ Perfil consultado com sucesso");

        // ===== ETAPA 4: ATUALIZAR INFORMAÇÕES DO PERFIL =====
        System.out.println("\n✏️ ETAPA 4: Usuário atualiza email e login");
        String profileUpdateJson = """
                {
                    "email": "marina.oliveira.profissional@healthapp.com",
                    "login": "marinapro",
                    "senha": "MinhaPrimeiraSenha@2024"
                }
                """;

        mockMvc.perform(put("/api/users/" + userId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileUpdateJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("marina.oliveira.profissional@healthapp.com"))
                .andExpect(jsonPath("$.login").value("marinapro"));
        System.out.println("✅ Perfil atualizado com sucesso");

        // ===== ETAPA 5: MUDAR SENHA =====
        System.out.println("\n🔒 ETAPA 5: Usuário decide mudar a senha");
        UserCredentialsRequestDto passwordChangeRequest = new UserCredentialsRequestDto(
                "marina.oliveira.profissional@healthapp.com",
                "MinhaNovaSenhaSegura@2025"
        );

        mockMvc.perform(patch("/api/v1/auth/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andDo(print())
                .andExpect(status().isNoContent());
        System.out.println("✅ Senha alterada com sucesso");

        // ===== ETAPA 6: VERIFICAR QUE SENHA ANTIGA NÃO FUNCIONA =====
        System.out.println("\n🚫 ETAPA 6: Verificando que senha antiga não funciona mais");
        UserCredentialsRequestDto oldPasswordAttempt = new UserCredentialsRequestDto(
                "marina.oliveira.profissional@healthapp.com",
                "MinhaPrimeiraSenha@2024"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oldPasswordAttempt)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        System.out.println("✅ Confirmado: senha antiga não funciona");

        // ===== ETAPA 7: LOGIN COM NOVA SENHA =====
        System.out.println("\n🔐 ETAPA 7: Login com nova senha");
        UserCredentialsRequestDto newPasswordLogin = new UserCredentialsRequestDto(
                "marina.oliveira.profissional@healthapp.com",
                "MinhaNovaSenhaSegura@2025"
        );

        MvcResult newLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPasswordLogin)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String newLoginResponse = newLoginResult.getResponse().getContentAsString();
        UserAuthRequestDto newAuthResponse = objectMapper.readValue(newLoginResponse, UserAuthRequestDto.class);
        System.out.println("✅ Login com nova senha bem-sucedido! Novo token JWT gerado");
        System.out.println("   Token: " + newAuthResponse.accessToken());

        // ===== ETAPA 8: LISTAR TODOS OS USUÁRIOS =====
        System.out.println("\n📋 ETAPA 8: Verificando lista de usuários");
        mockMvc.perform(get("/api/users")
                        .with(csrf())
                        .header("Authorization", "Bearer " + newAuthResponse.accessToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("marina.oliveira.profissional@healthapp.com"));
        System.out.println("✅ Lista de usuários consultada");

        // ===== ETAPA 9: USUÁRIO DECIDE DELETAR A CONTA =====
        System.out.println("\n🗑️ ETAPA 9: Usuário decide deletar sua conta");
        mockMvc.perform(delete("/api/users/" + userId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + newAuthResponse.accessToken()))
                .andDo(print())
                .andExpect(status().isNoContent());
        System.out.println("✅ Conta deletada com sucesso");

        // ===== ETAPA 10: VERIFICAR QUE CONTA NÃO EXISTE MAIS =====
        System.out.println("\n🔍 ETAPA 10: Verificando que conta não existe mais");
        mockMvc.perform(get("/api/users/" + userId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + newAuthResponse.accessToken()))
                .andDo(print())
                .andExpect(status().isNotFound());

        assertThat(userRepository.findById(userId)).isEmpty();
        System.out.println("✅ Confirmado: conta não existe mais no sistema");

        System.out.println("\n🎉🎉🎉 Jornada Realista Completa finalizada com sucesso! 🎉🎉🎉\n");
        System.out.println("Todas as funcionalidades da aplicação foram testadas de ponta a ponta:");
        System.out.println("  ✓ Registro de usuário");
        System.out.println("  ✓ Autenticação e geração de JWT");
        System.out.println("  ✓ Consulta de perfil");
        System.out.println("  ✓ Atualização de dados");
        System.out.println("  ✓ Mudança de senha");
        System.out.println("  ✓ Validação de credenciais");
        System.out.println("  ✓ Listagem de usuários");
        System.out.println("  ✓ Deleção de conta\n");
    }
}
