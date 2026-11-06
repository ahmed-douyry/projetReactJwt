package org.example.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StatisticsResponse(
        long totalUsers,
        @JsonProperty("EMPLOYEE") long employeeCount,
        @JsonProperty("TECHNICIAN") long technicianCount,
        @JsonProperty("ADMIN") long adminCount,
        long totalIncidents,
        @JsonProperty("OPEN") long openCount,
        @JsonProperty("ASSIGNED") long assignedCount,
        @JsonProperty("IN_PROGRESS") long inProgressCount,
        @JsonProperty("RESOLVED") long resolvedCount,
        @JsonProperty("CLOSED") long closedCount) {}
