package com.fiap.user.health.bff.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.user.health.bff.dto.request.UserRequestDto;
import com.fiap.user.health.bff.dto.request.UserUpdateRequestDto;
import com.fiap.user.health.bff.dto.response.UserResponseDto;
import com.fiap.user.health.bff.mapper.UserMapper;
import com.fiap.user.health.bff.model.User;
import com.fiap.user.health.bff.service.user.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(UserControllerIntegrationTest.TestConfig.class)
@DisplayName("User Controller - Testes de Integração")
class UserControllerIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    @SuppressWarnings("unused")
    private UserServiceInterface userService;

    @MockitoBean
    @SuppressWarnings("unused")
    private UserMapper userMapper;

    private UserRequestDto validUserRequest;
    private User user;
    private UserResponseDto userResponse;

    @BeforeEach
    void setUp() {
        validUserRequest = new UserRequestDto(
                "João Silva",
                "joao@email.com",
                "joaosilva",
                "senha123"
        );

        user = User.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao@email.com")
                .login("joaosilva")
                .senha("encodedPassword")
                .build();

        userResponse = new UserResponseDto(
                1L,
                "João Silva",
                "joao@email.com",
                "joaosilva"
        );
    }

    @Test
    @WithMockUser
    @DisplayName("Deve criar usuário com sucesso quando dados são válidos")
    void shouldCreateUserSuccessfully() throws Exception {
        // Arrange
        when(userMapper.toModel(any(UserRequestDto.class))).thenReturn(user);
        when(userService.createUser(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDto(any(User.class))).thenReturn(userResponse);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.login").value("joaosilva"));

        verify(userService).createUser(any(User.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar erro 400 quando nome é inválido")
    void shouldReturnBadRequestWhenNameIsInvalid() throws Exception {
        // Arrange
        UserRequestDto invalidRequest = new UserRequestDto(
                "A", // Nome muito curto
                "joao@email.com",
                "joaosilva",
                "senha123"
        );

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar erro 400 quando email é inválido")
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        // Arrange
        UserRequestDto invalidRequest = new UserRequestDto(
                "João Silva",
                "email-invalido", // Email sem formato válido
                "joaosilva",
                "senha123"
        );

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar erro 400 quando senha é muito curta")
    void shouldReturnBadRequestWhenPasswordIsTooShort() throws Exception {
        // Arrange
        UserRequestDto invalidRequest = new UserRequestDto(
                "João Silva",
                "joao@email.com",
                "joaosilva",
                "123" // Senha muito curta (mínimo 8 caracteres)
        );

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Deve listar todos os usuários com sucesso")
    void shouldGetAllUsersSuccessfully() throws Exception {
        // Arrange
        User user2 = User.builder()
                .id(2L)
                .nome("Maria Santos")
                .email("maria@email.com")
                .login("mariasantos")
                .senha("encodedPassword2")
                .build();

        UserResponseDto response2 = new UserResponseDto(
                2L,
                "Maria Santos",
                "maria@email.com",
                "mariasantos"
        );

        List<User> users = Arrays.asList(user, user2);

        when(userService.getAllUsers()).thenReturn(users);
        when(userMapper.toResponseDto(user)).thenReturn(userResponse);
        when(userMapper.toResponseDto(user2)).thenReturn(response2);

        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("João Silva"))
                .andExpect(jsonPath("$[1].nome").value("Maria Santos"));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void shouldGetUserByIdSuccessfully() throws Exception {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(userResponse);

        // Act & Assert
        mockMvc.perform(get("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));

        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 404 quando usuário não existe")
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        // Arrange
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(999L);
    }

    @Test
    @WithMockUser
    @DisplayName("Deve atualizar usuário com sucesso")
    void shouldUpdateUserSuccessfully() throws Exception {
        // Arrange
        UserUpdateRequestDto updateRequest = new UserUpdateRequestDto(
                "novoemail@email.com",
                "novoLogin",
                "novaSenha123"
        );

        User updatedUser = User.builder()
                .id(1L)
                .email("novoemail@email.com")
                .login("novoLogin")
                .senha("encodedNewPassword")
                .build();

        UserResponseDto updatedResponse = new UserResponseDto(
                1L,
                "João Silva",
                "novoemail@email.com",
                "novoLogin"
        );

        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(Optional.of(updatedUser));
        when(userMapper.toResponseDto(updatedUser)).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("novoemail@email.com"))
                .andExpect(jsonPath("$.login").value("novoLogin"));

        verify(userService).updateUser(eq(1L), any(User.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Deve deletar usuário com sucesso")
    void shouldDeleteUserSuccessfully() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar erro 400 quando todos os campos estão vazios")
    void shouldReturnBadRequestWhenAllFieldsAreEmpty() throws Exception {
        // Arrange
        String invalidJson = """
                {
                    "nome": "",
                    "email": "",
                    "login": "",
                    "senha": ""
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar erro 400 quando campos obrigatórios estão ausentes")
    void shouldReturnBadRequestWhenRequiredFieldsAreMissing() throws Exception {
        // Arrange
        String invalidJson = """
                {
                    "nome": "João Silva"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }
}
