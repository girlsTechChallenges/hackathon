package com.fiap.user.health.bff.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("SecurityConfig - Testes de Integração")
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    @DisplayName("Deve configurar SecurityFilterChain corretamente")
    void shouldConfigureSecurityFilterChain() {
        // Assert
        assertThat(securityFilterChain).isNotNull();
    }

    @Test
    @DisplayName("Deve configurar JwtEncoder bean")
    void shouldConfigureJwtEncoderBean() {
        // Assert
        assertThat(jwtEncoder).isNotNull();
    }

    @Test
    @DisplayName("Deve configurar JwtDecoder bean")
    void shouldConfigureJwtDecoderBean() {
        // Assert
        assertThat(jwtDecoder).isNotNull();
    }

    @Test
    @DisplayName("Deve configurar BCryptPasswordEncoder bean")
    void shouldConfigureBCryptPasswordEncoderBean() {
        // Assert
        assertThat(passwordEncoder).isNotNull();
    }

    @Test
    @DisplayName("Deve codificar senha corretamente")
    void shouldEncodePasswordCorrectly() {
        // Arrange
        String rawPassword = "senhaSegura123";

        // Act
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Assert
        assertThat(encodedPassword).isNotNull();
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(encodedPassword).startsWith("$2a$");
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    @Test
    @DisplayName("Deve configurar CORS corretamente")
    void shouldConfigureCors() {
        // Arrange
        var mockRequest = new org.springframework.mock.web.MockHttpServletRequest();
        mockRequest.setRequestURI("/api/users");

        // Assert
        assertThat(corsConfigurationSource).isNotNull();

        var corsConfig = corsConfigurationSource.getCorsConfiguration(mockRequest);
        assertThat(corsConfig).isNotNull();
        assertThat(corsConfig.getAllowedOrigins()).contains("*");
        assertThat(corsConfig.getAllowedMethods()).containsExactlyInAnyOrder(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        );
        assertThat(corsConfig.getAllowedHeaders()).contains("*");
        assertThat(corsConfig.getAllowCredentials()).isFalse();
    }

    @Test
    @DisplayName("Deve validar diferentes senhas com BCrypt")
    void shouldValidateDifferentPasswordsWithBCrypt() {
        // Arrange
        String password1 = "senha123";
        String password2 = "outraSenha456";

        // Act
        String encoded1 = passwordEncoder.encode(password1);
        String encoded2 = passwordEncoder.encode(password2);

        // Assert
        assertThat(passwordEncoder.matches(password1, encoded1)).isTrue();
        assertThat(passwordEncoder.matches(password2, encoded2)).isTrue();
        assertThat(passwordEncoder.matches(password1, encoded2)).isFalse();
        assertThat(passwordEncoder.matches(password2, encoded1)).isFalse();
    }

    @Test
    @DisplayName("Deve gerar hash diferente para mesma senha")
    void shouldGenerateDifferentHashForSamePassword() {
        // Arrange
        String password = "senhaQualquer123";

        // Act
        String encoded1 = passwordEncoder.encode(password);
        String encoded2 = passwordEncoder.encode(password);

        // Assert - Hashes diferentes devido ao salt
        assertThat(encoded1).isNotEqualTo(encoded2);
        // Mas ambos devem validar a senha original
        assertThat(passwordEncoder.matches(password, encoded1)).isTrue();
        assertThat(passwordEncoder.matches(password, encoded2)).isTrue();
    }
}
