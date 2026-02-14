package com.fiap.user.health.bff.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.user.health.bff.dto.request.UserAuthRequestDto;
import com.fiap.user.health.bff.dto.request.UserCredentialsRequestDto;
import com.fiap.user.health.bff.service.auth.AuthServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import(AuthControllerIntegrationTest.TestConfig.class)
@DisplayName("Auth Controller - Testes de Integração")
class AuthControllerIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        @org.springframework.context.annotation.Primary
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    @SuppressWarnings("unused")
    private AuthServiceInterface authService;

    private UserCredentialsRequestDto validCredentials;
    private UserAuthRequestDto authResponse;

    @BeforeEach
    void setUp() {
        validCredentials = new UserCredentialsRequestDto(
                "usuario@email.com",
                "senhaSegura123"
        );

        authResponse = new UserAuthRequestDto(
                "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvQGVtYWlsLmNvbSJ9.signature",
                3600L
        );
    }

    @Test
    @DisplayName("Deve realizar login com sucesso quando credenciais são válidas")
    void shouldLoginSuccessfullyWithValidCredentials() throws Exception {
        // Arrange
        when(authService.login(any(UserCredentialsRequestDto.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCredentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(authResponse.accessToken()))
                .andExpect(jsonPath("$.expiresIn").value(3600L));

        verify(authService).login(any(UserCredentialsRequestDto.class));
    }

    @Test
    @DisplayName("Deve retornar 401 quando credenciais são inválidas")
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        // Arrange
        when(authService.login(any(UserCredentialsRequestDto.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCredentials)))
                .andExpect(status().isUnauthorized());

        verify(authService).login(any(UserCredentialsRequestDto.class));
    }

    @Test
    @DisplayName("Deve retornar 400 quando email não é fornecido no login")
    void shouldReturnBadRequestWhenEmailIsMissingInLogin() throws Exception {
        // Arrange
        String invalidJson = """
                {
                    "password": "senha123"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(UserCredentialsRequestDto.class));
    }

    @Test
    @DisplayName("Deve retornar 400 quando senha não é fornecida no login")
    void shouldReturnBadRequestWhenPasswordIsMissingInLogin() throws Exception {
        // Arrange
        String invalidJson = """
                {
                    "email": "usuario@email.com"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(UserCredentialsRequestDto.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Deve atualizar senha com sucesso")
    void shouldUpdatePasswordSuccessfully() throws Exception {
        // Arrange
        UserCredentialsRequestDto passwordUpdate = new UserCredentialsRequestDto(
                "usuario@email.com",
                "novaSenhaSegura456"
        );

        doNothing().when(authService).updatePassword(anyString(), anyString());

        // Act & Assert
        mockMvc.perform(patch("/api/v1/auth/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordUpdate)))
                .andExpect(status().isNoContent());

        verify(authService).updatePassword("usuario@email.com", "novaSenhaSegura456");
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 400 quando dados de atualização de senha são inválidos")
    void shouldReturnBadRequestWhenPasswordUpdateDataIsInvalid() throws Exception {
        // Arrange
        String invalidJson = """
                {
                    "email": ""
                }
                """;

        // Act & Assert
        mockMvc.perform(patch("/api/v1/auth/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(authService, never()).updatePassword(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve retornar 400 quando formato de email é inválido no login")
    void shouldReturnBadRequestWhenEmailFormatIsInvalid() throws Exception {
        // Arrange
        UserCredentialsRequestDto invalidCredentials = new UserCredentialsRequestDto(
                "email-invalido",
                "senha123"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCredentials)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(UserCredentialsRequestDto.class));
    }

    @Test
    @DisplayName("Deve retornar token com tempo de expiração correto")
    void shouldReturnTokenWithCorrectExpirationTime() throws Exception {
        // Arrange
        when(authService.login(any(UserCredentialsRequestDto.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCredentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.expiresIn").value(3600));

        verify(authService).login(any(UserCredentialsRequestDto.class));
    }
}
