package com.fiap.user.health.bff.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCredentialsRequestDto(
        @NotNull @NotBlank @Email String email,
        @NotNull @NotBlank String password) {
}
