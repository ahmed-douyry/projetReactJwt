package org.example.backend.security;

import org.example.backend.domain.entity.User;
import org.example.backend.exception.ApiException;
import org.example.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new ApiException(401, "Session invalide");
        }
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ApiException(401, "Session invalide"));
        if (!user.isEnabled()) throw new ApiException(401, "Session invalide");
        return user;
    }
}
