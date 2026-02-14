package com.fiap.user.health.bff.service.auth;

import com.fiap.user.health.bff.dto.request.UserAuthRequestDto;
import com.fiap.user.health.bff.dto.request.UserCredentialsRequestDto;

public interface AuthServiceInterface {
    UserAuthRequestDto login(UserCredentialsRequestDto credentials);
    void updatePassword(String email, String newPassword);
}
