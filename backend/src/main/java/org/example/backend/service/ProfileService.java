package org.example.backend.service;

import org.example.backend.domain.entity.User;
import org.example.backend.dto.request.ChangePasswordRequest;
import org.example.backend.dto.request.UpdateProfileRequest;
import org.example.backend.dto.response.UserResponse;
import org.example.backend.exception.ApiException;
import org.example.backend.mapper.UserMapper;
import org.example.backend.repository.UserRepository;
import org.example.backend.security.CurrentUser;
import org.example.backend.util.EmailNormalizer;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@PreAuthorize("isAuthenticated()")
public class ProfileService {
    private final CurrentUser currentUser;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public ProfileService(
            CurrentUser currentUser,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper) {
        this.currentUser = currentUser;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public UserResponse getProfile() {
        return userMapper.toResponse(currentUser.get());
    }

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = currentUser.get();
        String email = EmailNormalizer.normalize(request.email());
        if (userRepository.existsByEmailAndIdNot(email, user.getId())) {
            throw new ApiException(409, "Cet email est déjà utilisé");
        }
        if (!user.getEmail().equals(email)) user.revokeSessions();
        user.setFullName(request.fullName().strip());
        user.setEmail(email);
        userRepository.flush();
        return userMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUser.get();
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new ApiException(400, "Ancien mot de passe incorrect");
        }
        if (!request.newPassword().equals(request.confirmation())) {
            throw new ApiException(400, "La confirmation ne correspond pas");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.revokeSessions();
    }
}
