package org.example.backend.service;

import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.Role;
import org.example.backend.domain.enums.Status;
import org.example.backend.dto.response.StatisticsResponse;
import org.example.backend.dto.response.UserResponse;
import org.example.backend.exception.ApiException;
import org.example.backend.mapper.UserMapper;
import org.example.backend.repository.IncidentRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.security.CurrentUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminService {
    private final UserRepository userRepository;
    private final IncidentRepository incidentRepository;
    private final CurrentUser currentUser;
    private final UserMapper userMapper;

    public AdminService(
            UserRepository userRepository,
            IncidentRepository incidentRepository,
            CurrentUser currentUser,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.incidentRepository = incidentRepository;
        this.currentUser = currentUser;
        this.userMapper = userMapper;
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAllByOrderByCreatedAtAscIdAsc().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse setUserEnabled(Long userId, boolean enabled) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ApiException(404, "Utilisateur introuvable"));
        if (!enabled && currentUser.get().getId().equals(userId)) {
            throw new ApiException(400, "Vous ne pouvez pas désactiver votre propre compte");
        }
        if (user.isEnabled() != enabled) {
            user.setEnabled(enabled);
            user.revokeSessions();
        }
        return userMapper.toResponse(user);
    }

    public StatisticsResponse getStatistics() {
        return new StatisticsResponse(
                userRepository.count(),
                userRepository.countByRole(Role.EMPLOYEE),
                userRepository.countByRole(Role.TECHNICIAN),
                userRepository.countByRole(Role.ADMIN),
                incidentRepository.count(),
                incidentRepository.countByStatus(Status.OPEN),
                incidentRepository.countByStatus(Status.ASSIGNED),
                incidentRepository.countByStatus(Status.IN_PROGRESS),
                incidentRepository.countByStatus(Status.RESOLVED),
                incidentRepository.countByStatus(Status.CLOSED));
    }
}
