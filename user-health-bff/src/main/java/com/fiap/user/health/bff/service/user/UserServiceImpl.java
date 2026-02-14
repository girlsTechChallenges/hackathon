package com.fiap.user.health.bff.service.user;

import com.fiap.user.health.bff.exception.EmailAlreadyExistsException;
import com.fiap.user.health.bff.exception.UserNotFoundException;
import com.fiap.user.health.bff.mapper.UserMapper;
import com.fiap.user.health.bff.model.User;
import com.fiap.user.health.bff.persistence.entity.UserEntity;
import com.fiap.user.health.bff.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserServiceInterface {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User createUser(User user) {
        log.info("Creating new user with email: {}", user.getEmail());

        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            log.warn("Email already exists: {}", user.getEmail());
            throw new EmailAlreadyExistsException(user.getEmail());
        }

        // Encrypt password before saving
        user = User.builder()
                .id(user.getId())
                .nome(user.getNome())
                .email(user.getEmail())
                .login(user.getLogin())
                .senha(passwordEncoder.encode(user.getSenha()))
                .build();

        UserEntity entity = userMapper.toEntity(user);
        UserEntity savedEntity = userRepository.save(entity);
        log.info("User created successfully with id: {}", savedEntity.getId());
        return userMapper.toModel(savedEntity);
    }

    @Override
    @Transactional
    public Optional<User> updateUser(Long id, User user) {
        log.info("Updating user with id: {}", id);

        UserEntity existingEntity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Check if email is being changed to an existing one
        if (!existingEntity.getEmail().equals(user.getEmail())) {
            userRepository.findByEmail(user.getEmail()).ifPresent(u -> {
                if (!u.getId().equals(id)) {
                    log.warn("Email already exists: {}", user.getEmail());
                    throw new EmailAlreadyExistsException(user.getEmail());
                }
            });
        }

        existingEntity.setEmail(user.getEmail());
        existingEntity.setLogin(user.getLogin());
        // Encrypt password before updating
        existingEntity.setSenha(passwordEncoder.encode(user.getSenha()));

        UserEntity updatedEntity = userRepository.save(existingEntity);
        log.info("User updated successfully with id: {}", id);
        return Optional.of(userMapper.toModel(updatedEntity));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(entity);

        log.info("User deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.debug("Fetching all users");

        List<User> users = userRepository.findAll().stream()
                .map(userMapper::toModel)
                .collect(Collectors.toList());

        log.debug("Found {} users", users.size());
        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        log.debug("Fetching user with id: {}", id);

        Optional<User> user = userRepository.findById(id)
                .map(userMapper::toModel);

        if (user.isPresent()) {
            log.debug("User found with id: {}", id);
        } else {
            log.debug("User not found with id: {}", id);
        }

        return user;
    }
}
