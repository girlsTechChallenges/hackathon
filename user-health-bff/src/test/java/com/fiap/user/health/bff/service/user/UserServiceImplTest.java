package com.fiap.user.health.bff.service.user;

import com.fiap.user.health.bff.exception.EmailAlreadyExistsException;
import com.fiap.user.health.bff.exception.UserNotFoundException;
import com.fiap.user.health.bff.mapper.UserMapper;
import com.fiap.user.health.bff.model.User;
import com.fiap.user.health.bff.persistence.entity.UserEntity;
import com.fiap.user.health.bff.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service - Testes Unitários")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        // Preparação dos dados de teste
        user = User.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao@email.com")
                .login("joaosilva")
                .senha("senha123")
                .build();

        userEntity = UserEntity.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao@email.com")
                .login("joaosilva")
                .senha("$2a$10$encodedPassword")
                .build();
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso quando email não existir")
    @SuppressWarnings("DataFlowIssue")
    void shouldCreateUserSuccessfully() {
        // Arrange - Configuração do cenário de teste
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(userMapper.toEntity(any(User.class))).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toModel(userEntity)).thenReturn(user);

        // Act - Execução do método testado
        User createdUser = userService.createUser(user);

        // Assert - Verificação dos resultados
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("joao@email.com");
        assertThat(createdUser.getNome()).isEqualTo("João Silva");

        // Verificação de interações com mocks
        verify(userRepository).findByEmail(user.getEmail());
        verify(passwordEncoder).encode("senha123");
        verify(userRepository).save(any(UserEntity.class));
        verify(userMapper).toEntity(any(User.class));
        verify(userMapper).toModel(userEntity);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar usuário com email já existente")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(userEntity));

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining(user.getEmail());

        // Verificação que o save não foi chamado
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso")
    void shouldUpdateUserSuccessfully() {
        // Arrange
        User updatedUser = User.builder()
                .email("novoemail@email.com")
                .login("novoLogin")
                .senha("novaSenha123")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.findByEmail(updatedUser.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$newEncodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toModel(userEntity)).thenReturn(user);

        // Act
        Optional<User> result = userService.updateUser(1L, updatedUser);

        // Assert
        assertThat(result).isPresent();
        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("novaSenha123");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar usuário inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(999L, user))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar usuário com email já existente de outro usuário")
    void shouldThrowExceptionWhenUpdatingWithExistingEmail() {
        // Arrange
        User updatedUser = User.builder()
                .email("existente@email.com")
                .login("novoLogin")
                .senha("novaSenha123")
                .build();

        UserEntity anotherUserEntity = UserEntity.builder()
                .id(2L)
                .email("existente@email.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.findByEmail("existente@email.com")).thenReturn(Optional.of(anotherUserEntity));

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(1L, updatedUser))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("existente@email.com");
    }

    @Test
    @DisplayName("Deve deletar usuário com sucesso")
    void shouldDeleteUserSuccessfully() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        doNothing().when(userRepository).delete(userEntity);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).findById(1L);
        verify(userRepository).delete(userEntity);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar usuário inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).delete(any(UserEntity.class));
    }

    @Test
    @DisplayName("Deve retornar lista de todos os usuários")
    void shouldGetAllUsers() {
        // Arrange
        UserEntity user2Entity = UserEntity.builder()
                .id(2L)
                .nome("Maria Santos")
                .email("maria@email.com")
                .login("mariasantos")
                .senha("$2a$10$encodedPassword2")
                .build();

        User user2 = User.builder()
                .id(2L)
                .nome("Maria Santos")
                .email("maria@email.com")
                .login("mariasantos")
                .senha("senha456")
                .build();

        List<UserEntity> entities = Arrays.asList(userEntity, user2Entity);

        when(userRepository.findAll()).thenReturn(entities);
        when(userMapper.toModel(userEntity)).thenReturn(user);
        when(userMapper.toModel(user2Entity)).thenReturn(user2);

        // Act
        List<User> users = userService.getAllUsers();

        // Assert
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("joao@email.com", "maria@email.com");

        verify(userRepository).findAll();
        verify(userMapper, times(2)).toModel(any(UserEntity.class));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver usuários")
    void shouldReturnEmptyListWhenNoUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of());

        // Act
        List<User> users = userService.getAllUsers();

        // Assert
        assertThat(users).isEmpty();
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar usuário ao buscar por ID existente")
    void shouldGetUserByIdWhenExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userMapper.toModel(userEntity)).thenReturn(user);

        // Act
        Optional<User> result = userService.getUserById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getEmail()).isEqualTo("joao@email.com");

        verify(userRepository).findById(1L);
        verify(userMapper).toModel(userEntity);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio ao buscar por ID inexistente")
    void shouldReturnEmptyWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserById(999L);

        // Assert
        assertThat(result).isEmpty();
        verify(userRepository).findById(999L);
        verify(userMapper, never()).toModel(any(UserEntity.class));
    }
}
