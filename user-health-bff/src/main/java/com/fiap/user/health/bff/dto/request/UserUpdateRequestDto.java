package com.fiap.user.health.bff.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDto(
        @NotNull
        @NotBlank
        @Email
        String email,

        @NotNull
        @NotBlank
        @Size(min = 5, max = 20)
        String login,

        @NotNull
        @NotBlank
        @Size(min = 8, max = 100)
        String senha) {
}
