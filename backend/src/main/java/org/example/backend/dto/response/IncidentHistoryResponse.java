package org.example.backend.dto.response;

import java.time.Instant;

public record IncidentHistoryResponse(
        Long id, String message, UserResponse author, Instant createdAt) {}
