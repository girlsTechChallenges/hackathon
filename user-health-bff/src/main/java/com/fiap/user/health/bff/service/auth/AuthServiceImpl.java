package com.fiap.user.health.bff.service.auth;

import com.fiap.user.health.bff.dto.request.UserAuthRequestDto;
import com.fiap.user.health.bff.dto.request.UserCredentialsRequestDto;
import com.fiap.user.health.bff.exception.UserNotFoundException;
import com.fiap.user.health.bff.persistence.entity.UserEntity;
import com.fiap.user.health.bff.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthServiceInterface {

    private final UserRepository userRepository;
    private final JwtEncoder jwtEncoder;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final long EXPIRATION_TIME = 3600L; // 1 hora em segundos

    @Override
    @Transactional(readOnly = true)
    public UserAuthRequestDto login(UserCredentialsRequestDto credentials) {
        log.info("Attempting login for email: {}", credentials.email());

        UserEntity user = userRepository.findByEmail(credentials.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(credentials.password(), user.getSenha())) {
            log.warn("Invalid password attempt for email: {}", credentials.email());
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = generateToken(user);
        log.info("Login successful for email: {}", credentials.email());

        return new UserAuthRequestDto(token, EXPIRATION_TIME);
    }

    @Override
    @Transactional
    public void updatePassword(String email, String newPassword) {
        log.info("Updating password for email: {}", email);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setSenha(encodedPassword);
        userRepository.save(user);

        log.info("Password updated successfully for email: {}", email);
    }

    private String generateToken(UserEntity user) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(EXPIRATION_TIME);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("user-health-bff")
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("nome", user.getNome())
                .issuedAt(now)
                .expiresAt(expiration)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
