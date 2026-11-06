package org.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddIncidentCommentRequest(@NotBlank @Size(max = 3000) String content) {}
