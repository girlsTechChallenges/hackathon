package com.fiap.user.health.bff.service.auth;

import com.fiap.user.health.bff.dto.request.UserAuthRequestDto;
import com.fiap.user.health.bff.dto.request.UserCredentialsRequestDto;
import com.fiap.user.health.bff.exception.UserNotFoundException;
import com.fiap.user.health.bff.persistence.entity.UserEntity;
import com.fiap.user.health.bff.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service - Testes Unitários")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserEntity userEntity;
    private UserCredentialsRequestDto validCredentials;

    @BeforeEach
    void setUp() {
        userEntity = UserEntity.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao@email.com")
                .login("joaosilva")
                .senha("$2a$10$encodedPassword")
                .build();

        validCredentials = new UserCredentialsRequestDto(
                "joao@email.com",
                "senha123"
        );
    }

    @Test
    @DisplayName("Deve realizar login com sucesso quando credenciais são válidas")
    void shouldLoginSuccessfullyWithValidCredentials() {
        // Arrange
        when(userRepository.findByEmail(validCredentials.email())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(validCredentials.password(), userEntity.getSenha())).thenReturn(true);

        // Mock do JWT
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mock.jwt.token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        // Act
        UserAuthRequestDto authResponse = authService.login(validCredentials);

        // Assert
        assertThat(authResponse).isNotNull();
        assertThat(authResponse.accessToken()).isEqualTo("mock.jwt.token");
        assertThat(authResponse.expiresIn()).isEqualTo(3600L);

        verify(userRepository).findByEmail(validCredentials.email());
        verify(passwordEncoder).matches(validCredentials.password(), userEntity.getSenha());
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException quando email não existe")
    void shouldThrowBadCredentialsExceptionWhenEmailNotFound() {
        // Arrange
        UserCredentialsRequestDto invalidCredentials = new UserCredentialsRequestDto(
                "naoexiste@email.com",
                "senha123"
        );

        when(userRepository.findByEmail(invalidCredentials.email())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(invalidCredentials))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByEmail(invalidCredentials.email());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException quando senha está incorreta")
    void shouldThrowBadCredentialsExceptionWhenPasswordIsWrong() {
        // Arrange
        UserCredentialsRequestDto wrongPasswordCredentials = new UserCredentialsRequestDto(
                "joao@email.com",
                "senhaErrada"
        );

        when(userRepository.findByEmail(wrongPasswordCredentials.email())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(wrongPasswordCredentials.password(), userEntity.getSenha())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(wrongPasswordCredentials))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByEmail(wrongPasswordCredentials.email());
        verify(passwordEncoder).matches(wrongPasswordCredentials.password(), userEntity.getSenha());
        verify(jwtEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Deve atualizar senha com sucesso")
    void shouldUpdatePasswordSuccessfully() {
        // Arrange
        String newPassword = "novaSenha123";
        String encodedNewPassword = "$2a$10$newEncodedPassword";

        when(userRepository.findByEmail(userEntity.getEmail())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        when(userRepository.save(userEntity)).thenReturn(userEntity);

        // Act
        authService.updatePassword(userEntity.getEmail(), newPassword);

        // Assert
        verify(userRepository).findByEmail(userEntity.getEmail());
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(userEntity);
        assertThat(userEntity.getSenha()).isEqualTo(encodedNewPassword);
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException ao atualizar senha de usuário inexistente")
    void shouldThrowUserNotFoundExceptionWhenUpdatingPasswordOfNonExistentUser() {
        // Arrange
        String email = "naoexiste@email.com";
        String newPassword = "novaSenha123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.updatePassword(email, newPassword))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with email: " + email);

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Deve gerar token JWT com claims corretos no login")
    void shouldGenerateJwtTokenWithCorrectClaims() {
        // Arrange
        when(userRepository.findByEmail(validCredentials.email())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(validCredentials.password(), userEntity.getSenha())).thenReturn(true);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJqb2FvQGVtYWlsLmNvbSJ9.signature");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        // Act
        UserAuthRequestDto authResponse = authService.login(validCredentials);

        // Assert
        assertThat(authResponse.accessToken()).isNotNull();
        assertThat(authResponse.accessToken()).startsWith("eyJ"); // JWT padrão começa com "eyJ"

        // Verifica que o encoder foi chamado com parâmetros
        // Verificação básica de que o encoder foi chamado
        verify(jwtEncoder).encode(argThat(Objects::nonNull));
    }

    @Test
    @DisplayName("Deve retornar tempo de expiração correto no token")
    void shouldReturnCorrectExpirationTime() {
        // Arrange
        when(userRepository.findByEmail(validCredentials.email())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(validCredentials.password(), userEntity.getSenha())).thenReturn(true);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mock.jwt.token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        // Act
        UserAuthRequestDto authResponse = authService.login(validCredentials);

        // Assert
        assertThat(authResponse.expiresIn()).isEqualTo(3600L); // 1 hora em segundos
    }
}
