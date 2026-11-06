package org.example.backend.web.controller;

import jakarta.validation.Valid;

import org.example.backend.dto.request.AddIncidentCommentRequest;
import org.example.backend.dto.request.CreateIncidentRequest;
import org.example.backend.dto.request.UpdateIncidentStatusRequest;
import org.example.backend.dto.response.IncidentResponse;
import org.example.backend.service.IncidentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentResponse createIncident(@Valid @RequestBody CreateIncidentRequest request) {
        return incidentService.create(request);
    }

    @GetMapping
    public List<IncidentResponse> listAllIncidents() {
        return incidentService.listAll();
    }

    @GetMapping("/my")
    public List<IncidentResponse> listMyIncidents() {
        return incidentService.listMine();
    }

    @GetMapping("/assigned")
    public List<IncidentResponse> listAssignedIncidents() {
        return incidentService.listAssigned();
    }

    @GetMapping("/{id}")
    public IncidentResponse getIncident(@PathVariable Long id) {
        return incidentService.detail(id);
    }

    @PutMapping("/{id}/assign/{technicianId}")
    public IncidentResponse assignIncident(@PathVariable Long id, @PathVariable Long technicianId) {
        return incidentService.assign(id, technicianId);
    }

    @PutMapping("/{id}/status")
    public IncidentResponse updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateIncidentStatusRequest request) {
        return incidentService.status(id, request.status());
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentResponse addComment(
            @PathVariable Long id, @Valid @RequestBody AddIncidentCommentRequest request) {
        return incidentService.comment(id, request.content());
    }
}
