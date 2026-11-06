package org.example.backend.dto.response;

import org.example.backend.domain.enums.Role;

import java.time.Instant;

public record AuthResponse(
        String token,
        String refreshToken,
        Instant expiresAt,
        Role role,
        String fullName,
        UserResponse user) {}
