package com.fiap.user.health.bff.controller;

import com.fiap.user.health.bff.controller.docs.AuthControllerDocs;
import com.fiap.user.health.bff.dto.request.UserAuthRequestDto;
import com.fiap.user.health.bff.dto.request.UserCredentialsRequestDto;
import com.fiap.user.health.bff.service.auth.AuthServiceInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthServiceInterface authService;

    @Override
    @PostMapping("/login")
    public ResponseEntity<UserAuthRequestDto> login(@Valid @RequestBody UserCredentialsRequestDto loginRequest) {
        log.info("Login request received for email: {}", loginRequest.email());
        UserAuthRequestDto authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @Override
    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UserCredentialsRequestDto request) {
        log.info("Password update request received for email: {}", request.email());
        authService.updatePassword(request.email(), request.password());
        return ResponseEntity.noContent().build();
    }
}
