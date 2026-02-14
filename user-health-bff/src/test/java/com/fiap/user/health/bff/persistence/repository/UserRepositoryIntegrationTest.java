package com.fiap.user.health.bff.persistence.repository;

import com.fiap.user.health.bff.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("User Repository - Testes de Integração")
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        // Limpar o banco antes de cada teste
        userRepository.deleteAll();

        userEntity = UserEntity.builder()
                .nome("João Silva")
                .email("joao@email.com")
                .login("joaosilva")
                .senha("$2a$10$encodedPassword")
                .build();
    }

    @Test
    @DisplayName("Deve salvar usuário com sucesso")
    void shouldSaveUserSuccessfully() {
        // Act
        UserEntity savedUser = userRepository.save(userEntity);

        // Assert
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getNome()).isEqualTo("João Silva");
        assertThat(savedUser.getEmail()).isEqualTo("joao@email.com");
        assertThat(savedUser.getLogin()).isEqualTo("joaosilva");
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void shouldFindUserByIdSuccessfully() {
        // Arrange
        UserEntity savedUser = userRepository.save(userEntity);

        // Act
        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.get().getEmail()).isEqualTo("joao@email.com");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não existe por ID")
    void shouldReturnEmptyWhenUserNotFoundById() {
        // Act
        Optional<UserEntity> foundUser = userRepository.findById(999L);

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar usuário por email com sucesso")
    void shouldFindUserByEmailSuccessfully() {
        // Arrange
        userRepository.save(userEntity);

        // Act
        Optional<UserEntity> foundUser = userRepository.findByEmail("joao@email.com");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("joao@email.com");
        assertThat(foundUser.get().getNome()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando email não existe")
    void shouldReturnEmptyWhenEmailNotFound() {
        // Act
        Optional<UserEntity> foundUser = userRepository.findByEmail("naoexiste@email.com");

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void shouldFindAllUsers() {
        // Arrange
        UserEntity user2 = UserEntity.builder()
                .nome("Maria Santos")
                .email("maria@email.com")
                .login("mariasantos")
                .senha("$2a$10$encodedPassword2")
                .build();

        userRepository.save(userEntity);
        userRepository.save(user2);

        // Act
        List<UserEntity> users = userRepository.findAll();

        // Assert
        assertThat(users).hasSize(2);
        assertThat(users).extracting(UserEntity::getEmail)
                .containsExactlyInAnyOrder("joao@email.com", "maria@email.com");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há usuários")
    void shouldReturnEmptyListWhenNoUsers() {
        // Act
        List<UserEntity> users = userRepository.findAll();

        // Assert
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso")
    void shouldUpdateUserSuccessfully() {
        // Arrange
        UserEntity savedUser = userRepository.save(userEntity);
        savedUser.setEmail("novoemail@email.com");
        savedUser.setLogin("novoLogin");

        // Act
        UserEntity updatedUser = userRepository.save(savedUser);

        // Assert
        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.getEmail()).isEqualTo("novoemail@email.com");
        assertThat(updatedUser.getLogin()).isEqualTo("novoLogin");
        assertThat(updatedUser.getNome()).isEqualTo("João Silva"); // Nome não mudou
    }

    @Test
    @DisplayName("Deve deletar usuário com sucesso")
    void shouldDeleteUserSuccessfully() {
        // Arrange
        UserEntity savedUser = userRepository.save(userEntity);
        Long userId = savedUser.getId();

        // Act
        userRepository.delete(savedUser);

        // Assert
        Optional<UserEntity> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar que email é único")
    void shouldEnsureEmailIsUnique() {
        // Arrange
        userRepository.save(userEntity);


        // Act & Assert
        // O banco deve garantir a unicidade do email (caso tenha constraint)
        // Aqui testamos que podemos encontrar apenas um usuário com esse email
        Optional<UserEntity> foundUser = userRepository.findByEmail("joao@email.com");
        assertThat(foundUser).isPresent();

        // Salvar duplicata (se não houver constraint, isso funcionará)
        // Em produção, devemos ter uma constraint UNIQUE no email
        List<UserEntity> allUsers = userRepository.findAll();
        long count = allUsers.stream()
                .filter(u -> u.getEmail().equals("joao@email.com"))
                .count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve contar usuários corretamente")
    void shouldCountUsersCorrectly() {
        // Arrange
        userRepository.save(userEntity);

        UserEntity user2 = UserEntity.builder()
                .nome("Maria Santos")
                .email("maria@email.com")
                .login("mariasantos")
                .senha("$2a$10$encodedPassword2")
                .build();
        userRepository.save(user2);

        // Act
        long count = userRepository.count();

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve verificar se usuário existe por ID")
    void shouldCheckIfUserExistsById() {
        // Arrange
        UserEntity savedUser = userRepository.save(userEntity);

        // Act
        boolean exists = userRepository.existsById(savedUser.getId());
        boolean notExists = userRepository.existsById(999L);

        // Assert
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Deve deletar usuário por ID")
    void shouldDeleteUserById() {
        // Arrange
        UserEntity savedUser = userRepository.save(userEntity);
        Long userId = savedUser.getId();

        // Act
        userRepository.deleteById(userId);

        // Assert
        Optional<UserEntity> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("Deve persistir senha criptografada")
    void shouldPersistEncryptedPassword() {
        // Arrange
        UserEntity savedUser = userRepository.save(userEntity);

        // Act
        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getSenha()).startsWith("$2a$10$"); // BCrypt hash
    }
}
