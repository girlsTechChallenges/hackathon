package com.fiap.user.health.bff.controller;

import com.fiap.user.health.bff.controller.docs.UserControllerDocs;
import com.fiap.user.health.bff.dto.request.UserRequestDto;
import com.fiap.user.health.bff.dto.request.UserUpdateRequestDto;
import com.fiap.user.health.bff.dto.response.UserResponseDto;
import com.fiap.user.health.bff.exception.UserNotFoundException;
import com.fiap.user.health.bff.mapper.UserMapper;
import com.fiap.user.health.bff.model.User;
import com.fiap.user.health.bff.service.user.UserServiceInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserControllerDocs {

    private final UserServiceInterface userService;
    private final UserMapper userMapper;

    @Override
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        User user = userMapper.toModel(userRequestDto);
        User createdUser = userService.createUser(user);
        UserResponseDto response = userMapper.toResponseDto(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers().stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        UserResponseDto response = userMapper.toResponseDto(user);
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDto updateRequestDto) {

        User user = User.builder()
                .email(updateRequestDto.email())
                .login(updateRequestDto.login())
                .senha(updateRequestDto.senha())
                .build();

        User updatedUser = userService.updateUser(id, user)
                .orElseThrow(() -> new UserNotFoundException(id));

        UserResponseDto response = userMapper.toResponseDto(updatedUser);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
