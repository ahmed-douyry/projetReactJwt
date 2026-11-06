package org.example.backend.mapper;

import org.example.backend.domain.entity.Incident;
import org.example.backend.domain.entity.IncidentComment;
import org.example.backend.domain.entity.IncidentHistory;
import org.example.backend.dto.response.IncidentCommentResponse;
import org.example.backend.dto.response.IncidentHistoryResponse;
import org.example.backend.dto.response.IncidentResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IncidentMapper {
    private final UserMapper userMapper;

    public IncidentMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public IncidentResponse toSummary(Incident incident) {
        return toResponse(incident, List.of(), List.of());
    }

    public IncidentResponse toDetails(Incident incident) {
        List<IncidentHistoryResponse> history =
                incident.getHistory().stream().map(this::toHistoryResponse).toList();
        List<IncidentCommentResponse> comments =
                incident.getComments().stream().map(this::toCommentResponse).toList();
        return toResponse(incident, history, comments);
    }

    private IncidentResponse toResponse(
            Incident incident,
            List<IncidentHistoryResponse> history,
            List<IncidentCommentResponse> comments) {
        return new IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getCategory(),
                incident.getPriority(),
                incident.getStatus(),
                incident.getCreatedAt(),
                incident.getUpdatedAt(),
                userMapper.toResponse(incident.getCreatedBy()),
                userMapper.toResponse(incident.getAssignedTo()),
                history,
                comments);
    }

    private IncidentHistoryResponse toHistoryResponse(IncidentHistory history) {
        return new IncidentHistoryResponse(
                history.getId(),
                history.getMessage(),
                userMapper.toResponse(history.getAuthor()),
                history.getCreatedAt());
    }

    private IncidentCommentResponse toCommentResponse(IncidentComment comment) {
        return new IncidentCommentResponse(
                comment.getId(),
                comment.getContent(),
                userMapper.toResponse(comment.getAuthor()),
                comment.getCreatedAt());
    }
}
