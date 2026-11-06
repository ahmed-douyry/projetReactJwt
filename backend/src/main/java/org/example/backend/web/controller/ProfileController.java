package org.example.backend.web.controller;

import jakarta.validation.Valid;

import org.example.backend.dto.request.ChangePasswordRequest;
import org.example.backend.dto.request.UpdateProfileRequest;
import org.example.backend.dto.response.MessageResponse;
import org.example.backend.dto.response.UserResponse;
import org.example.backend.service.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public UserResponse getProfile() {
        return profileService.getProfile();
    }

    @PutMapping
    public UserResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return profileService.updateProfile(request);
    }

    @PutMapping("/password")
    public MessageResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(request);
        return new MessageResponse("Mot de passe modifié. Reconnectez-vous.");
    }
}
