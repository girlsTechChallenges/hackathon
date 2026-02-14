package com.fiap.user.health.bff.mapper;

import com.fiap.user.health.bff.dto.request.UserRequestDto;
import com.fiap.user.health.bff.dto.response.UserResponseDto;
import com.fiap.user.health.bff.model.User;
import com.fiap.user.health.bff.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Mapper - Testes Unitários")
class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    @DisplayName("Deve converter UserRequestDto para User Model corretamente")
    void shouldConvertUserRequestDtoToModel() {
        // Arrange
        UserRequestDto dto = new UserRequestDto(
                "João Silva",
                "joao@email.com",
                "joaosilva",
                "senha123"
        );

        // Act
        User user = userMapper.toModel(dto);

        // Assert
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull(); // ID não vem do DTO de criação
        assertThat(user.getNome()).isEqualTo("João Silva");
        assertThat(user.getEmail()).isEqualTo("joao@email.com");
        assertThat(user.getLogin()).isEqualTo("joaosilva");
        assertThat(user.getSenha()).isEqualTo("senha123");
    }

    @Test
    @DisplayName("Deve converter UserEntity para User Model corretamente")
    void shouldConvertUserEntityToModel() {
        // Arrange
        UserEntity entity = UserEntity.builder()
                .id(1L)
                .nome("Maria Santos")
                .email("maria@email.com")
                .login("mariasantos")
                .senha("$2a$10$encodedPassword")
                .build();

        // Act
        User user = userMapper.toModel(entity);

        // Assert
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getNome()).isEqualTo("Maria Santos");
        assertThat(user.getEmail()).isEqualTo("maria@email.com");
        assertThat(user.getLogin()).isEqualTo("mariasantos");
        assertThat(user.getSenha()).isEqualTo("$2a$10$encodedPassword");
    }

    @Test
    @DisplayName("Deve converter User Model para UserEntity corretamente")
    void shouldConvertUserModelToEntity() {
        // Arrange
        User user = User.builder()
                .id(2L)
                .nome("Carlos Oliveira")
                .email("carlos@email.com")
                .login("carlosoliveira")
                .senha("senha456")
                .build();

        // Act
        UserEntity entity = userMapper.toEntity(user);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getNome()).isEqualTo("Carlos Oliveira");
        assertThat(entity.getEmail()).isEqualTo("carlos@email.com");
        assertThat(entity.getLogin()).isEqualTo("carlosoliveira");
        assertThat(entity.getSenha()).isEqualTo("senha456");
    }

    @Test
    @DisplayName("Deve converter User Model para UserResponseDto corretamente")
    void shouldConvertUserModelToResponseDto() {
        // Arrange
        User user = User.builder()
                .id(3L)
                .nome("Ana Paula")
                .email("ana@email.com")
                .login("anapaula")
                .senha("$2a$10$encodedPassword") // Senha não deve aparecer no DTO de resposta
                .build();

        // Act
        UserResponseDto dto = userMapper.toResponseDto(user);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(3L);
        assertThat(dto.nome()).isEqualTo("Ana Paula");
        assertThat(dto.email()).isEqualTo("ana@email.com");
        assertThat(dto.login()).isEqualTo("anapaula");
        // Senha não deve estar presente no DTO de resposta
    }

    @Test
    @DisplayName("Deve converter User Model sem ID para UserEntity")
    void shouldConvertUserModelWithoutIdToEntity() {
        // Arrange - Simula um novo usuário antes de salvar
        User user = User.builder()
                .nome("Pedro Costa")
                .email("pedro@email.com")
                .login("pedrocosta")
                .senha("senha789")
                .build();

        // Act
        UserEntity entity = userMapper.toEntity(user);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getNome()).isEqualTo("Pedro Costa");
        assertThat(entity.getEmail()).isEqualTo("pedro@email.com");
        assertThat(entity.getLogin()).isEqualTo("pedrocosta");
        assertThat(entity.getSenha()).isEqualTo("senha789");
    }

    @Test
    @DisplayName("Deve preservar todos os campos na conversão bidirecional Model <-> Entity")
    void shouldPreserveAllFieldsInBidirectionalConversion() {
        // Arrange
        User originalUser = User.builder()
                .id(10L)
                .nome("Teste Completo")
                .email("teste@email.com")
                .login("testecompleto")
                .senha("senhaSegura123")
                .build();

        // Act
        UserEntity entity = userMapper.toEntity(originalUser);
        User convertedUser = userMapper.toModel(entity);

        // Assert
        assertThat(convertedUser).isNotNull();
        assertThat(convertedUser.getId()).isEqualTo(originalUser.getId());
        assertThat(convertedUser.getNome()).isEqualTo(originalUser.getNome());
        assertThat(convertedUser.getEmail()).isEqualTo(originalUser.getEmail());
        assertThat(convertedUser.getLogin()).isEqualTo(originalUser.getLogin());
        assertThat(convertedUser.getSenha()).isEqualTo(originalUser.getSenha());
    }

    @Test
    @DisplayName("Deve lidar com nomes com caracteres especiais")
    void shouldHandleSpecialCharactersInName() {
        // Arrange
        UserRequestDto dto = new UserRequestDto(
                "José María Ñoño",
                "jose@email.com",
                "josemaria",
                "senha123"
        );

        // Act
        User user = userMapper.toModel(dto);

        // Assert
        assertThat(user).isNotNull();
        assertThat(user.getNome()).isEqualTo("José María Ñoño");
    }

    @Test
    @DisplayName("Deve converter UserEntity com senha criptografada para Model")
    void shouldConvertEntityWithEncodedPasswordToModel() {
        // Arrange
        UserEntity entity = UserEntity.builder()
                .id(5L)
                .nome("Usuário Seguro")
                .email("seguro@email.com")
                .login("usuarioseguro")
                .senha("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy")
                .build();

        // Act
        User user = userMapper.toModel(entity);

        // Assert
        assertThat(user).isNotNull();
        assertThat(user.getSenha()).startsWith("$2a$10$"); // BCrypt hash
        assertThat(user.getSenha()).hasSize(60); // BCrypt hash padrão tem 60 caracteres
    }
}
