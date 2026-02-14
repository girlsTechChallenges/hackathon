package com.fiap.user.health.bff.dto.request;

public record UserAuthRequestDto(String accessToken, Long expiresIn) {
}
