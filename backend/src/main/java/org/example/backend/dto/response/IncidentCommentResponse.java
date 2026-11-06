package org.example.backend.dto.response;

import java.time.Instant;

public record IncidentCommentResponse(
        Long id, String content, UserResponse author, Instant createdAt) {}
