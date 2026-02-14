package com.fiap.user.health.bff.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.user.health.bff.dto.request.UserCredentialsRequestDto;
import com.fiap.user.health.bff.dto.request.UserRequestDto;
import com.fiap.user.health.bff.dto.request.UserUpdateRequestDto;
import com.fiap.user.health.bff.dto.response.UserResponseDto;
import com.fiap.user.health.bff.persistence.entity.UserEntity;
import com.fiap.user.health.bff.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Integração REAIS - Controllers + Services + Repository")
class RealIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Limpar o banco antes de cada teste
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Integração REAL: Deve cadastrar usuário e persistir no banco")
    @WithMockUser
    void shouldCreateUserAndPersistInDatabase() throws Exception {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        UserRequestDto createRequest = new UserRequestDto(
                "Ana Paula Silva",
                "ana.silva@email.com",
                "anapaula",
                "senhaSegura123"
        );

        // Act - Cadastrar através da API
        MvcResult result = mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Ana Paula Silva"))
                .andExpect(jsonPath("$.email").value("ana.silva@email.com"))
                .andExpect(jsonPath("$.login").value("anapaula"))
                .andReturn();

        // Assert - Verificar que foi persistido no banco com query real
        String responseJson = result.getResponse().getContentAsString();
        UserResponseDto createdUser = objectMapper.readValue(responseJson, UserResponseDto.class);

        UserEntity savedEntity = userRepository.findById(createdUser.id()).orElseThrow();
        assertThat(savedEntity.getNome()).isEqualTo("Ana Paula Silva");
        assertThat(savedEntity.getEmail()).isEqualTo("ana.silva@email.com");
        assertThat(savedEntity.getLogin()).isEqualTo("anapaula");
        assertThat(savedEntity.getSenha()).startsWith("$2a$10$"); // Senha criptografada

        // Verificar que a senha foi criptografada corretamente
        assertThat(passwordEncoder.matches("senhaSegura123", savedEntity.getSenha())).isTrue();
    }

    @Test
    @DisplayName("Integração REAL: Deve listar todos os usuários do banco")
    @WithMockUser
    void shouldListAllUsersFromDatabase() throws Exception {
        // Arrange - Inserir usuários diretamente no banco
        UserEntity user1 = UserEntity.builder()
                .nome("João Santos")
                .email("joao@email.com")
                .login("joaosantos")
                .senha(passwordEncoder.encode("senha123"))
                .build();

        UserEntity user2 = UserEntity.builder()
                .nome("Maria Oliveira")
                .email("maria@email.com")
                .login("mariaoliveira")
                .senha(passwordEncoder.encode("senha456"))
                .build();

        UserEntity user3 = UserEntity.builder()
                .nome("Pedro Costa")
                .email("pedro@email.com")
                .login("pedrocosta")
                .senha(passwordEncoder.encode("senha789"))
                .build();

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        // Act & Assert - Listar através da API e verificar query real
        mockMvc.perform(get("/api/users")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].nome", containsInAnyOrder(
                        "João Santos", "Maria Oliveira", "Pedro Costa")))
                .andExpect(jsonPath("$[*].email", containsInAnyOrder(
                        "joao@email.com", "maria@email.com", "pedro@email.com")));

        // Verificar que a query real retornou os dados do banco
        long countInDb = userRepository.count();
        assertThat(countInDb).isEqualTo(3);
    }

    @Test
    @DisplayName("Integração REAL: Deve buscar usuário por ID com query real")
    @WithMockUser
    void shouldFindUserByIdWithRealQuery() throws Exception {
        // Arrange - Inserir usuário no banco
        UserEntity savedUser = userRepository.save(UserEntity.builder()
                .nome("Carlos Eduardo")
                .email("carlos@email.com")
                .login("carloseduardo")
                .senha(passwordEncoder.encode("senha123"))
                .build());

        Long userId = savedUser.getId();

        // Act & Assert - Buscar através da API com query real
        mockMvc.perform(get("/api/users/" + userId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.nome").value("Carlos Eduardo"))
                .andExpect(jsonPath("$.email").value("carlos@email.com"))
                .andExpect(jsonPath("$.login").value("carloseduardo"));

        // Verificar que o usuário existe no banco
        assertThat(userRepository.existsById(userId)).isTrue();
    }

    @Test
    @DisplayName("Integração REAL: Deve atualizar usuário e persistir mudanças no banco")
    @WithMockUser
    void shouldUpdateUserAndPersistChanges() throws Exception {
        // Arrange - Criar usuário inicial
        UserEntity originalUser = userRepository.save(UserEntity.builder()
                .nome("Fernanda Lima")
                .email("fernanda@email.com")
                .login("fernandalima")
                .senha(passwordEncoder.encode("senhaAntiga123"))
                .build());

        Long userId = originalUser.getId();

        // Act - Atualizar através da API
        ObjectMapper objectMapper = new ObjectMapper();
        UserUpdateRequestDto updateRequest = new UserUpdateRequestDto(
                "fernanda.nova@email.com",
                "fernandanova",
                "senhaNova456"
        );

        mockMvc.perform(put("/api/users/" + userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("fernanda.nova@email.com"))
                .andExpect(jsonPath("$.login").value("fernandanova"));

        // Assert - Verificar que foi atualizado no banco com query real
        UserEntity updatedUser = userRepository.findById(userId).orElseThrow();
        assertThat(updatedUser.getEmail()).isEqualTo("fernanda.nova@email.com");
        assertThat(updatedUser.getLogin()).isEqualTo("fernandanova");
        assertThat(passwordEncoder.matches("senhaNova456", updatedUser.getSenha())).isTrue();
        assertThat(updatedUser.getNome()).isEqualTo("Fernanda Lima"); // Nome não mudou
    }

    @Test
    @DisplayName("Integração REAL: Deve atualizar senha e verificar no banco")
    @WithMockUser
    void shouldUpdatePasswordAndVerifyInDatabase() throws Exception {
        // Arrange - Criar usuário
        UserEntity user = userRepository.save(UserEntity.builder()
                .nome("Roberto Silva")
                .email("roberto@email.com")
                .login("robertosilva")
                .senha(passwordEncoder.encode("senhaAntiga123"))
                .build());

        Long userId = user.getId();

        // Act - Atualizar senha através da API
        ObjectMapper objectMapper = new ObjectMapper();
        UserCredentialsRequestDto updatePasswordRequest = new UserCredentialsRequestDto(
                "roberto@email.com",
                "senhaNovaSuperSegura456"
        );

        mockMvc.perform(patch("/api/v1/auth/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePasswordRequest)))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Assert - Verificar que a senha foi atualizada no banco
        UserEntity updatedUser = userRepository.findByEmail("roberto@email.com").orElseThrow();
        assertThat(passwordEncoder.matches("senhaNovaSuperSegura456", updatedUser.getSenha()))
                .isTrue();
        assertThat(passwordEncoder.matches("senhaAntiga123", updatedUser.getSenha()))
                .isFalse();
        assertThat(updatedUser.getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Integração REAL: Deve realizar login e retornar JWT válido")
    void shouldLoginAndReturnValidJWT() throws Exception {
        // Arrange - Criar usuário no banco
        userRepository.save(UserEntity.builder()
                .nome("Login Test User")
                .email("login@email.com")
                .login("loginuser")
                .senha(passwordEncoder.encode("senhaParaLogin123"))
                .build());

        // Act - Realizar login através da API
        ObjectMapper objectMapper = new ObjectMapper();
        UserCredentialsRequestDto loginRequest = new UserCredentialsRequestDto(
                "login@email.com",
                "senhaParaLogin123"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andReturn();

        // Assert - Verificar que o JWT foi gerado
        String responseJson = result.getResponse().getContentAsString();
        assertThat(responseJson).contains("accessToken");
        assertThat(responseJson).contains("expiresIn");

        // Extrair e validar estrutura do JWT
        String token = objectMapper.readTree(responseJson).get("accessToken").asText();
        assertThat(token).isNotEmpty();
        assertThat(token).contains("."); // JWT tem 3 partes separadas por ponto

        String[] jwtParts = token.split("\\.");
        assertThat(jwtParts).hasSize(3); // Header.Payload.Signature
    }

    @Test
    @DisplayName("Integração REAL: Deve falhar login com senha incorreta")
    void shouldFailLoginWithWrongPassword() throws Exception {
        // Arrange
        userRepository.save(UserEntity.builder()
                .nome("Test User")
                .email("test@email.com")
                .login("testuser")
                .senha(passwordEncoder.encode("senhaCorreta123"))
                .build());

        // Act & Assert - Tentar login com senha errada
        ObjectMapper objectMapper = new ObjectMapper();
        UserCredentialsRequestDto wrongPasswordLogin = new UserCredentialsRequestDto(
                "test@email.com",
                "senhaErrada123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPasswordLogin)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Integração REAL: Deve deletar usuário e remover do banco")
    @WithMockUser
    void shouldDeleteUserAndRemoveFromDatabase() throws Exception {
        // Arrange - Criar usuário
        UserEntity user = userRepository.save(UserEntity.builder()
                .nome("Delete Test")
                .email("delete@email.com")
                .login("deletetest")
                .senha(passwordEncoder.encode("senha123"))
                .build());

        Long userId = user.getId();
        assertThat(userRepository.existsById(userId)).isTrue();

        // Act - Deletar através da API
        mockMvc.perform(delete("/api/users/" + userId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Assert - Verificar que foi removido do banco com query real
        assertThat(userRepository.existsById(userId)).isFalse();
        assertThat(userRepository.findById(userId)).isEmpty();
    }

    @Test
    @DisplayName("Integração REAL: Deve impedir cadastro com email duplicado")
    @WithMockUser
    void shouldPreventDuplicateEmailRegistration() throws Exception {
        // Arrange - Criar primeiro usuário
        userRepository.save(UserEntity.builder()
                .nome("First User")
                .email("duplicate@email.com")
                .login("firstuser")
                .senha(passwordEncoder.encode("senha123"))
                .build());

        // Act & Assert - Tentar criar segundo usuário com mesmo email
        ObjectMapper objectMapper = new ObjectMapper();
        UserRequestDto duplicateEmailRequest = new UserRequestDto(
                "Second User",
                "duplicate@email.com", // Email duplicado
                "seconduser",
                "senha456"
        );

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateEmailRequest)))
                .andDo(print())
                .andExpect(status().isConflict());

        // Verificar que apenas um usuário existe no banco
        long count = userRepository.count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Integração REAL: Deve buscar usuário por email com query customizada")
    @WithMockUser
    void shouldFindUserByEmailWithCustomQuery() {
        // Arrange - Criar usuário
        userRepository.save(UserEntity.builder()
                .nome("Email Search Test")
                .email("search@email.com")
                .login("searchtest")
                .senha(passwordEncoder.encode("senha123"))
                .build());

        // Act - Buscar através do repository (query real)
        UserEntity foundUser = userRepository.findByEmail("search@email.com").orElseThrow();

        // Assert - Verificar dados retornados da query
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("search@email.com");
        assertThat(foundUser.getNome()).isEqualTo("Email Search Test");
        assertThat(foundUser.getLogin()).isEqualTo("searchtest");
    }

    @Test
    @DisplayName("Integração REAL: Deve listar usuários vazios quando banco está vazio")
    @WithMockUser
    void shouldReturnEmptyListWhenDatabaseIsEmpty() throws Exception {
        // Assert que o banco está vazio
        assertThat(userRepository.count()).isEqualTo(0);

        // Act & Assert - Listar usuários
        mockMvc.perform(get("/api/users")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(jsonPath("$", empty()));
    }

    @Test
    @DisplayName("Integração REAL: Deve retornar 404 ao buscar usuário inexistente")
    @WithMockUser
    void shouldReturn404WhenUserNotFound() throws Exception {
        // Assert que o banco está vazio
        assertThat(userRepository.count()).isEqualTo(0);

        // Act & Assert - Buscar usuário inexistente
        mockMvc.perform(get("/api/users/999")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integração REAL: Deve validar formato de email no cadastro")
    @WithMockUser
    void shouldValidateEmailFormatOnRegistration() throws Exception {
        // Arrange - Email inválido
        ObjectMapper objectMapper = new ObjectMapper();
        UserRequestDto invalidEmailRequest = new UserRequestDto(
                "Invalid Email User",
                "email-sem-arroba", // Email inválido
                "invaliduser",
                "senhaValida123"
        );

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Verificar que nenhum usuário foi criado
        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Integração REAL: Deve validar senha mínima no cadastro")
    @WithMockUser
    void shouldValidateMinimumPasswordLengthOnRegistration() throws Exception {
        // Arrange - Senha muito curta
        ObjectMapper objectMapper = new ObjectMapper();
        UserRequestDto shortPasswordRequest = new UserRequestDto(
                "Short Password User",
                "valid@email.com",
                "validuser",
                "123" // Senha muito curta (mínimo 8)
        );

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortPasswordRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Verificar que nenhum usuário foi criado
        assertThat(userRepository.count()).isEqualTo(0);
    }
}
