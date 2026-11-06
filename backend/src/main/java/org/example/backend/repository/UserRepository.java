package org.example.backend.repository;

import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    long countByRole(Role role);

    List<User> findAllByOrderByCreatedAtAscIdAsc();
}
