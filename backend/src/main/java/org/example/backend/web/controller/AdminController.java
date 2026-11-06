package org.example.backend.web.controller;

import org.example.backend.dto.response.StatisticsResponse;
import org.example.backend.dto.response.UserResponse;
import org.example.backend.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public List<UserResponse> listUsers() {
        return adminService.listUsers();
    }

    @PutMapping("/users/{id}/enable")
    public UserResponse enableUser(@PathVariable Long id) {
        return adminService.setUserEnabled(id, true);
    }

    @PutMapping("/users/{id}/disable")
    public UserResponse disableUser(@PathVariable Long id) {
        return adminService.setUserEnabled(id, false);
    }

    @GetMapping("/statistics")
    public StatisticsResponse getStatistics() {
        return adminService.getStatistics();
    }
}
