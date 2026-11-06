package org.example.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.Role;
import org.example.backend.repository.IncidentRepository;
import org.example.backend.repository.RefreshTokenRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.security.JwtService;
import org.example.backend.service.AdminService;
import org.example.backend.service.IncidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:security-tests;DB_CLOSE_DELAY=-1",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "app.demo.enabled=false",
            "logging.level.root=WARN",
            "debug=false"
        })
@AutoConfigureMockMvc
class BackendApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired IncidentRepository incidents;
    @Autowired RefreshTokenRepository refreshTokens;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtService jwt;
    @Autowired AdminService adminService;
    @Autowired IncidentService incidentService;
    User employee, other, tech, otherTech, admin;
    String employeeToken, otherToken, techToken, otherTechToken, adminToken;

    @BeforeEach
    void setup() {
        refreshTokens.deleteAll();
        incidents.deleteAll();
        users.deleteAll();
        employee = user("Ahmed", "employee@test.ma", Role.EMPLOYEE);
        other = user("Salma", "other@test.ma", Role.EMPLOYEE);
        tech = user("Yassine", "tech@test.ma", Role.TECHNICIAN);
        otherTech = user("Nadia", "tech2@test.ma", Role.TECHNICIAN);
        admin = user("Sara", "admin@test.ma", Role.ADMIN);
        employeeToken = token(employee);
        otherToken = token(other);
        techToken = token(tech);
        otherTechToken = token(otherTech);
        adminToken = token(admin);
    }

    User user(String name, String email, Role role) {
        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setRole(role);
        u.setPassword(encoder.encode("Password123!"));
        return users.save(u);
    }

    String token(User u) {
        return jwt.create(u, Instant.now().plusSeconds(300));
    }

    JsonNode body(MvcResult r) throws Exception {
        return json.readTree(r.getResponse().getContentAsString());
    }

    String data(Object value) throws Exception {
        return json.writeValueAsString(value);
    }

    long create(String token, String title) throws Exception {
        return body(mvc.perform(
                                post("/api/incidents")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                data(
                                                        Map.of(
                                                                "title",
                                                                title,
                                                                "description",
                                                                "Description détaillée de"
                                                                        + " l’incident",
                                                                "category",
                                                                "NETWORK",
                                                                "priority",
                                                                "HIGH"))))
                        .andExpect(status().isCreated())
                        .andReturn())
                .get("id")
                .asLong();
    }

    ResultActions assign(long id, User technician) throws Exception {
        return mvc.perform(
                put("/api/incidents/" + id + "/assign/" + technician.getId())
                        .header("Authorization", "Bearer " + adminToken));
    }

    ResultActions changeStatus(long id, String token, String next) throws Exception {
        return mvc.perform(
                put("/api/incidents/" + id + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data(Map.of("status", next))));
    }

    @Test
    void protectedApiWithoutToken() throws Exception {
        mvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void invalidTokenRejected() throws Exception {
        mvc.perform(get("/api/profile").header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredTokenRejected() throws Exception {
        String expired = jwt.create(employee, Instant.now().minusSeconds(60));
        mvc.perform(get("/api/profile").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tamperedSignatureRejected() throws Exception {
        String fake =
                Jwts.builder()
                        .subject(employee.getEmail())
                        .claim("uid", employee.getId())
                        .claim("role", "ADMIN")
                        .claim("version", 0)
                        .expiration(Date.from(Instant.now().plusSeconds(60)))
                        .signWith(
                                Keys.hmacShaKeyFor(
                                        "a-different-signing-secret-at-least-32-bytes-long"
                                                .getBytes(StandardCharsets.UTF_8)))
                        .compact();
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + fake))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void employeeCannotReadAdminRoutes() throws Exception {
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeeCannotReadOtherIncident() throws Exception {
        long id = create(otherToken, "Incident privé");
        mvc.perform(get("/api/incidents/" + id).header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void technicianCannotChangeUnassignedIncident() throws Exception {
        long id = create(employeeToken, "Incident");
        assign(id, tech).andExpect(status().isOk());
        changeStatus(id, otherTechToken, "IN_PROGRESS").andExpect(status().isForbidden());
        mvc.perform(get("/api/incidents/" + id).header("Authorization", "Bearer " + otherTechToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void disabledUserCannotLoginOrUseExistingToken() throws Exception {
        mvc.perform(
                        put("/api/admin/users/" + employee.getId() + "/disable")
                                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        mvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        data(
                                                Map.of(
                                                        "email",
                                                        employee.getEmail(),
                                                        "password",
                                                        "Password123!"))))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/profile").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isUnauthorized());
        mvc.perform(
                        put("/api/admin/users/" + employee.getId() + "/enable")
                                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        mvc.perform(get("/api/profile").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateEmailRejectedIgnoringCase() throws Exception {
        mvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        data(
                                                Map.of(
                                                        "fullName",
                                                        "Test",
                                                        "email",
                                                        "EMPLOYEE@test.ma",
                                                        "password",
                                                        "Password123!"))))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidIncidentRejected() throws Exception {
        mvc.perform(
                        post("/api/incidents")
                                .header("Authorization", "Bearer " + employeeToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"\",\"description\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.priority").exists());
    }

    @Test
    void signupCannotEscalateRoleAndPasswordIsBcrypt() throws Exception {
        mvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        data(
                                                Map.of(
                                                        "fullName",
                                                        "Nouveau",
                                                        "email",
                                                        "new@test.ma",
                                                        "password",
                                                        "Password123!",
                                                        "role",
                                                        "ADMIN",
                                                        "enabled",
                                                        false))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.user.password").doesNotExist());
        User u = users.findByEmail("new@test.ma").orElseThrow();
        assertThat(u.getRole()).isEqualTo(Role.EMPLOYEE);
        assertThat(u.isEnabled()).isTrue();
        assertThat(encoder.matches("Password123!", u.getPassword())).isTrue();
        assertThat(u.getPassword()).startsWith("$2");
    }

    @Test
    void employeeListContainsOnlyOwnIncidents() throws Exception {
        create(employeeToken, "Mon incident");
        create(otherToken, "Autre incident");
        mvc.perform(get("/api/incidents/my").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Mon incident"));
    }

    @Test
    void onlyAdminCanAssign() throws Exception {
        long id = create(employeeToken, "Incident");
        mvc.perform(
                        put("/api/incidents/" + id + "/assign/" + tech.getId())
                                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
        assign(id, employee).andExpect(status().isBadRequest());
    }

    @Test
    void employeeCannotChangeStatusOrWriteNotes() throws Exception {
        long id = create(employeeToken, "Incident");
        assign(id, tech);
        changeStatus(id, employeeToken, "IN_PROGRESS").andExpect(status().isForbidden());
        mvc.perform(
                        post("/api/incidents/" + id + "/comments")
                                .header("Authorization", "Bearer " + employeeToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"content\":\"Note\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void fullLifecycleRetainsHistoryAndNotesAndLocksClosedIncident() throws Exception {
        long id = create(employeeToken, "Incident réseau");
        assign(id, tech)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
        mvc.perform(get("/api/incidents/assigned").header("Authorization", "Bearer " + techToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        changeStatus(id, techToken, "IN_PROGRESS").andExpect(status().isOk());
        mvc.perform(
                        post("/api/incidents/" + id + "/comments")
                                .header("Authorization", "Bearer " + techToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"content\":\"Analyse effectuée\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comments[0].content").value("Analyse effectuée"))
                .andExpect(jsonPath("$.comments[0].author.fullName").value("Yassine"));
        changeStatus(id, techToken, "RESOLVED").andExpect(status().isOk());
        mvc.perform(get("/api/incidents/" + id).header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.history.length()").value(5))
                .andExpect(jsonPath("$.createdBy.fullName").value("Ahmed"));
        changeStatus(id, adminToken, "CLOSED").andExpect(status().isOk());
        changeStatus(id, adminToken, "IN_PROGRESS").andExpect(status().isConflict());
        assign(id, otherTech).andExpect(status().isConflict());
    }

    @Test
    void invalidTransitionRejected() throws Exception {
        long id = create(employeeToken, "Incident");
        assign(id, tech);
        changeStatus(id, techToken, "CLOSED").andExpect(status().isBadRequest());
    }

    @Test
    void missingIncidentReturns404() throws Exception {
        mvc.perform(get("/api/incidents/999999").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCannotDisableSelf() throws Exception {
        mvc.perform(
                        put("/api/admin/users/" + admin.getId() + "/disable")
                                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void incorrectLoginReturns401() throws Exception {
        mvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        data(
                                                Map.of(
                                                        "email",
                                                        employee.getEmail(),
                                                        "password",
                                                        "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void passwordRequiresOldPasswordAndRevokesTokens() throws Exception {
        mvc.perform(
                        put("/api/profile/password")
                                .header("Authorization", "Bearer " + employeeToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        data(
                                                Map.of(
                                                        "oldPassword",
                                                        "wrong",
                                                        "newPassword",
                                                        "NewPassword!",
                                                        "confirmation",
                                                        "NewPassword!"))))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        put("/api/profile/password")
                                .header("Authorization", "Bearer " + employeeToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        data(
                                                Map.of(
                                                        "oldPassword",
                                                        "Password123!",
                                                        "newPassword",
                                                        "NewPassword!",
                                                        "confirmation",
                                                        "NewPassword!"))))
                .andExpect(status().isOk());
        mvc.perform(get("/api/profile").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profileEmailChangeRevokesOldToken() throws Exception {
        mvc.perform(
                        put("/api/profile")
                                .header("Authorization", "Bearer " + employeeToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        data(
                                                Map.of(
                                                        "fullName",
                                                        "Ahmed modifié",
                                                        "email",
                                                        "changed@test.ma"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("changed@test.ma"));
        mvc.perform(get("/api/profile").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshRotatesAndLogoutRevokesBothTokens() throws Exception {
        JsonNode login =
                body(
                        mvc.perform(
                                        post("/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                        data(
                                                                Map.of(
                                                                        "email",
                                                                        employee.getEmail(),
                                                                        "password",
                                                                        "Password123!"))))
                                .andExpect(status().isOk())
                                .andReturn());
        String raw = login.get("refreshToken").asText();
        JsonNode renewed =
                body(
                        mvc.perform(
                                        post("/auth/refresh")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(data(Map.of("refreshToken", raw))))
                                .andExpect(status().isOk())
                                .andReturn());
        mvc.perform(
                        post("/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(data(Map.of("refreshToken", raw))))
                .andExpect(status().isUnauthorized());
        mvc.perform(
                        post("/api/logout")
                                .header("Authorization", "Bearer " + renewed.get("token").asText()))
                .andExpect(status().isOk());
        mvc.perform(
                        get("/api/profile")
                                .header("Authorization", "Bearer " + renewed.get("token").asText()))
                .andExpect(status().isUnauthorized());
        mvc.perform(
                        post("/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        data(
                                                Map.of(
                                                        "refreshToken",
                                                        renewed.get("refreshToken").asText()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void statisticsReflectDatabase() throws Exception {
        create(employeeToken, "Incident");
        mvc.perform(get("/api/admin/statistics").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(5))
                .andExpect(jsonPath("$.EMPLOYEE").value(2))
                .andExpect(jsonPath("$.TECHNICIAN").value(2))
                .andExpect(jsonPath("$.ADMIN").value(1))
                .andExpect(jsonPath("$.ASSIGNED").value(0))
                .andExpect(jsonPath("$.IN_PROGRESS").value(0))
                .andExpect(jsonPath("$.RESOLVED").value(0))
                .andExpect(jsonPath("$.CLOSED").value(0))
                .andExpect(jsonPath("$.totalIncidents").value(1))
                .andExpect(jsonPath("$.OPEN").value(1));
    }

    @Test
    void corsAllowsFrontend() throws Exception {
        mvc.perform(
                        options("/api/profile")
                                .header("Origin", "http://localhost:5173")
                                .header("Access-Control-Request-Method", "GET")
                                .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void serviceLayerRejectsEmployeeAdministrativeActions() {
        var authentication =
                new UsernamePasswordAuthenticationToken(
                        employee.getId(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            assertThatThrownBy(adminService::listUsers).isInstanceOf(AccessDeniedException.class);
            assertThatThrownBy(incidentService::listAll).isInstanceOf(AccessDeniedException.class);
            assertThatThrownBy(() -> incidentService.assign(1L, tech.getId()))
                    .isInstanceOf(AccessDeniedException.class);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
