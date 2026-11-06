package org.example.backend.service;

import org.example.backend.domain.entity.RefreshToken;
import org.example.backend.domain.entity.User;
import org.example.backend.exception.ApiException;
import org.example.backend.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@Transactional
public class RefreshTokenService {
    private static final Duration LIFETIME = Duration.ofDays(7);
    private final SecureRandom random = new SecureRandom();
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String issue(User user) {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RefreshToken token = new RefreshToken();
        token.setTokenHash(hash(rawToken));
        token.setUser(user);
        token.setAuthVersion(user.getAuthVersion());
        token.setExpiresAt(Instant.now().plus(LIFETIME));
        refreshTokenRepository.save(token);
        return rawToken;
    }

    public User consume(String rawToken) {
        RefreshToken token =
                refreshTokenRepository
                        .findByTokenHash(hash(rawToken))
                        .orElseThrow(() -> new ApiException(401, "Refresh token invalide"));
        User user = token.getUser();
        if (token.isRevoked()
                || !token.getExpiresAt().isAfter(Instant.now())
                || !user.isEnabled()
                || user.getAuthVersion() != token.getAuthVersion()) {
            throw new ApiException(401, "Refresh token expiré ou révoqué");
        }
        token.setRevoked(true);
        return user;
    }

    private String hash(String value) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponible", exception);
        }
    }
}
