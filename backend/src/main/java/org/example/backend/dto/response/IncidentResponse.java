package org.example.backend.dto.response;

import org.example.backend.domain.enums.Category;
import org.example.backend.domain.enums.Priority;
import org.example.backend.domain.enums.Status;

import java.time.Instant;
import java.util.List;

public record IncidentResponse(
        Long id,
        String title,
        String description,
        Category category,
        Priority priority,
        Status status,
        Instant createdAt,
        Instant updatedAt,
        UserResponse createdBy,
        UserResponse assignedTo,
        List<IncidentHistoryResponse> history,
        List<IncidentCommentResponse> comments) {}
