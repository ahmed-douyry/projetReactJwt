package org.example.backend.dto.request;

import jakarta.validation.constraints.NotNull;

import org.example.backend.domain.enums.Status;

public record UpdateIncidentStatusRequest(@NotNull Status status) {}
