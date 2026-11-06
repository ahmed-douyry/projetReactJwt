package org.example.backend.service;

import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.Role;
import org.example.backend.dto.request.LoginRequest;
import org.example.backend.dto.request.RefreshTokenRequest;
import org.example.backend.dto.request.RegisterRequest;
import org.example.backend.dto.response.AuthResponse;
import org.example.backend.exception.ApiException;
import org.example.backend.mapper.UserMapper;
import org.example.backend.repository.UserRepository;
import org.example.backend.security.CurrentUser;
import org.example.backend.security.JwtService;
import org.example.backend.util.EmailNormalizer;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final CurrentUser currentUser;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            UserMapper userMapper,
            CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userMapper = userMapper;
        this.currentUser = currentUser;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(409, "Cet email est déjà utilisé");
        }
        User user = new User();
        user.setFullName(request.fullName().strip());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.EMPLOYEE);
        userRepository.saveAndFlush(user);
        return createSession(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user =
                userRepository
                        .findByEmail(EmailNormalizer.normalize(request.email()))
                        .orElseThrow(
                                () -> new ApiException(401, "Email ou mot de passe incorrect"));
        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException(401, "Email ou mot de passe incorrect, ou compte désactivé");
        }
        return createSession(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        User user = refreshTokenService.consume(request.refreshToken());
        return createSession(user);
    }

    @PreAuthorize("isAuthenticated()")
    public void logout() {
        currentUser.get().revokeSessions();
    }

    private AuthResponse createSession(User user) {
        Instant expiresAt = jwtService.expiration();
        return new AuthResponse(
                jwtService.create(user, expiresAt),
                refreshTokenService.issue(user),
                expiresAt,
                user.getRole(),
                user.getFullName(),
                userMapper.toResponse(user));
    }
}
