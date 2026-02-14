package com.fiap.user.health.bff.mapper;

import com.fiap.user.health.bff.dto.request.UserRequestDto;
import com.fiap.user.health.bff.dto.request.UserUpdateRequestDto;
import com.fiap.user.health.bff.dto.response.UserResponseDto;
import com.fiap.user.health.bff.model.User;
import com.fiap.user.health.bff.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toModel(UserRequestDto dto) {
        return User.builder()
                .nome(dto.nome())
                .email(dto.email())
                .login(dto.login())
                .senha(dto.senha())
                .build();
    }

    public User toModel(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .email(entity.getEmail())
                .login(entity.getLogin())
                .senha(entity.getSenha())
                .build();
    }

    public UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .nome(user.getNome())
                .email(user.getEmail())
                .login(user.getLogin())
                .senha(user.getSenha())
                .build();
    }

    public UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getLogin()
        );
    }

    public void updateEntityFromDto(UserEntity entity, UserUpdateRequestDto dto) {
        entity.setEmail(dto.email());
        entity.setLogin(dto.login());
        entity.setSenha(dto.senha());
    }
}
