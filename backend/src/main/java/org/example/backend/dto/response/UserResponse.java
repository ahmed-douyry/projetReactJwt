package org.example.backend.dto.response;

import org.example.backend.domain.enums.Role;

import java.time.Instant;

public record UserResponse(
        Long id, String fullName, String email, Role role, boolean enabled, Instant createdAt) {}
