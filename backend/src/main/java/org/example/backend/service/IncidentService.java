package org.example.backend.service;

import org.example.backend.domain.entity.Incident;
import org.example.backend.domain.entity.IncidentComment;
import org.example.backend.domain.entity.IncidentHistory;
import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.Role;
import org.example.backend.domain.enums.Status;
import org.example.backend.dto.request.CreateIncidentRequest;
import org.example.backend.dto.response.IncidentResponse;
import org.example.backend.exception.ApiException;
import org.example.backend.mapper.IncidentMapper;
import org.example.backend.repository.IncidentRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.security.CurrentUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@PreAuthorize("isAuthenticated()")
public class IncidentService {
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final IncidentMapper incidentMapper;

    public IncidentService(
            IncidentRepository incidentRepository,
            UserRepository userRepository,
            CurrentUser currentUser,
            IncidentMapper incidentMapper) {
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.incidentMapper = incidentMapper;
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<IncidentResponse> listMine() {
        return incidentRepository
                .findByCreatedByIdOrderByCreatedAtDesc(currentUser.get().getId())
                .stream()
                .map(incidentMapper::toSummary)
                .toList();
    }

    @PreAuthorize("hasRole('TECHNICIAN')")
    public List<IncidentResponse> listAssigned() {
        return incidentRepository
                .findByAssignedToIdOrderByCreatedAtDesc(currentUser.get().getId())
                .stream()
                .map(incidentMapper::toSummary)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<IncidentResponse> listAll() {
        return incidentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(incidentMapper::toSummary)
                .toList();
    }

    public IncidentResponse detail(Long incidentId) {
        Incident incident = findIncident(incidentId);
        checkAccess(incident, currentUser.get());
        return incidentMapper.toDetails(incident);
    }

    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public IncidentResponse create(CreateIncidentRequest request) {
        User creator = currentUser.get();
        Incident incident = new Incident();
        incident.setTitle(request.title().strip());
        incident.setDescription(request.description().strip());
        incident.setCategory(request.category());
        incident.setPriority(request.priority());
        incident.setCreatedBy(creator);
        recordHistory(incident, creator, "Incident créé");
        incidentRepository.saveAndFlush(incident);
        return incidentMapper.toDetails(incident);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public IncidentResponse assign(Long incidentId, Long technicianId) {
        Incident incident = findIncident(incidentId);
        checkNotClosed(incident, "Un incident clôturé ne peut plus être affecté");
        User technician =
                userRepository
                        .findById(technicianId)
                        .orElseThrow(() -> new ApiException(404, "Technicien introuvable"));
        if (technician.getRole() != Role.TECHNICIAN || !technician.isEnabled()) {
            throw new ApiException(400, "Choisissez un technicien actif");
        }
        if (incident.getAssignedTo() != null
                && incident.getAssignedTo().getId().equals(technicianId)) {
            throw new ApiException(409, "Incident déjà affecté à ce technicien");
        }
        incident.setAssignedTo(technician);
        if (incident.getStatus() == Status.OPEN) incident.setStatus(Status.ASSIGNED);
        recordHistory(incident, currentUser.get(), "Affecté à " + technician.getFullName());
        incidentRepository.flush();
        return incidentMapper.toDetails(incident);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    public IncidentResponse status(Long incidentId, Status nextStatus) {
        Incident incident = findIncident(incidentId);
        User actor = currentUser.get();
        checkAccess(incident, actor);
        checkNotClosed(incident, "Un incident clôturé ne peut plus être modifié");
        if (!allowedTransitions(incident.getStatus()).contains(nextStatus)) {
            throw new ApiException(400, "Transition de statut non autorisée");
        }
        if (incident.getAssignedTo() == null) {
            throw new ApiException(400, "Affectez un technicien avant de commencer le traitement");
        }
        Status previousStatus = incident.getStatus();
        incident.setStatus(nextStatus);
        recordHistory(incident, actor, previousStatus + " → " + nextStatus);
        incidentRepository.flush();
        return incidentMapper.toDetails(incident);
    }

    @Transactional
    @PreAuthorize("hasRole('TECHNICIAN')")
    public IncidentResponse comment(Long incidentId, String content) {
        Incident incident = findIncident(incidentId);
        User author = currentUser.get();
        checkAccess(incident, author);
        checkNotClosed(incident, "Un incident clôturé ne peut plus recevoir de notes");
        IncidentComment comment = new IncidentComment();
        comment.setAuthor(author);
        comment.setContent(content.strip());
        incident.addComment(comment);
        recordHistory(incident, author, "Note de traitement ajoutée");
        incidentRepository.flush();
        return incidentMapper.toDetails(incident);
    }

    private Incident findIncident(Long incidentId) {
        return incidentRepository
                .findById(incidentId)
                .orElseThrow(() -> new ApiException(404, "Incident introuvable"));
    }

    private void checkAccess(Incident incident, User user) {
        boolean allowed =
                switch (user.getRole()) {
                    case ADMIN -> true;
                    case EMPLOYEE -> incident.getCreatedBy().getId().equals(user.getId());
                    case TECHNICIAN ->
                            incident.getAssignedTo() != null
                                    && incident.getAssignedTo().getId().equals(user.getId());
                };
        if (!allowed) throw new ApiException(403, "Cet incident ne vous est pas accessible");
    }

    private void checkNotClosed(Incident incident, String message) {
        if (incident.getStatus() == Status.CLOSED) throw new ApiException(409, message);
    }

    private Set<Status> allowedTransitions(Status currentStatus) {
        return switch (currentStatus) {
            case OPEN, ASSIGNED -> Set.of(Status.IN_PROGRESS);
            case IN_PROGRESS -> Set.of(Status.RESOLVED);
            case RESOLVED -> Set.of(Status.CLOSED, Status.IN_PROGRESS);
            case CLOSED -> Set.of();
        };
    }

    private void recordHistory(Incident incident, User author, String message) {
        IncidentHistory event = new IncidentHistory();
        event.setAuthor(author);
        event.setMessage(message);
        incident.addHistory(event);
        incident.setUpdatedAt(Instant.now());
    }
}
