package org.example.backend.repository;

import org.example.backend.domain.entity.Incident;
import org.example.backend.domain.enums.Status;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    @EntityGraph(attributePaths = {"createdBy", "assignedTo"})
    List<Incident> findByCreatedByIdOrderByCreatedAtDesc(Long id);

    @EntityGraph(attributePaths = {"createdBy", "assignedTo"})
    List<Incident> findByAssignedToIdOrderByCreatedAtDesc(Long id);

    @EntityGraph(attributePaths = {"createdBy", "assignedTo"})
    List<Incident> findAllByOrderByCreatedAtDesc();

    long countByStatus(Status status);
}
