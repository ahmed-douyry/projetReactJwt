package org.example.backend.web.controller;

import org.example.backend.dto.response.MessageResponse;
import org.example.backend.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SessionController {
    private final AuthService authService;

    public SessionController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/logout")
    public MessageResponse logout() {
        authService.logout();
        return new MessageResponse("Déconnexion effectuée");
    }
}
