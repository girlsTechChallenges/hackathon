package com.fiap.user.health.bff.service.user;

import com.fiap.user.health.bff.model.User;

import java.util.List;
import java.util.Optional;

public interface UserServiceInterface {

    User createUser(User user);
    Optional<User> updateUser(Long id, User user);
    void deleteUser(Long id);
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
}
