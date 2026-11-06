package org.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.example.backend.domain.enums.Category;
import org.example.backend.domain.enums.Priority;

public record CreateIncidentRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 5000) String description,
        @NotNull Category category,
        @NotNull Priority priority) {}
