package org.example.backend.bootstrap;

import org.example.backend.domain.entity.Incident;
import org.example.backend.domain.entity.IncidentComment;
import org.example.backend.domain.entity.IncidentHistory;
import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.Category;
import org.example.backend.domain.enums.Priority;
import org.example.backend.domain.enums.Role;
import org.example.backend.domain.enums.Status;
import org.example.backend.repository.IncidentRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoData implements CommandLineRunner {
    private final UserRepository users;
    private final IncidentRepository incidents;
    private final PasswordEncoder encoder;

    public DemoData(UserRepository users, IncidentRepository incidents, PasswordEncoder encoder) {
        this.users = users;
        this.incidents = incidents;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.count() > 0) return;
        user("Sara El Amrani", "admin@sentinel.ma", Role.ADMIN);
        User tech = user("Yassine Bennani", "technicien@sentinel.ma", Role.TECHNICIAN);
        User first = user("Ahmed Benali", "employe@sentinel.ma", Role.EMPLOYEE);
        User second = user("Salma Idrissi", "employe2@sentinel.ma", Role.EMPLOYEE);
        ticket(
                first,
                null,
                "Email suspect imitant le service RH",
                Category.PHISHING,
                Priority.HIGH,
                Status.OPEN,
                25);
        ticket(
                first,
                tech,
                "Poste de travail infecté par un malware",
                Category.MALWARE,
                Priority.CRITICAL,
                Status.IN_PROGRESS,
                48);
        ticket(
                second,
                null,
                "Connexion Wi-Fi instable au deuxième étage",
                Category.NETWORK,
                Priority.MEDIUM,
                Status.OPEN,
                8);
        ticket(
                first,
                tech,
                "Accès à la messagerie bloqué",
                Category.ACCESS,
                Priority.HIGH,
                Status.ASSIGNED,
                5);
        ticket(
                second,
                tech,
                "Mise à jour du logiciel de comptabilité",
                Category.SOFTWARE,
                Priority.LOW,
                Status.RESOLVED,
                72);
        ticket(
                first,
                tech,
                "Écran externe non détecté",
                Category.HARDWARE,
                Priority.LOW,
                Status.CLOSED,
                96);
        ticket(
                second,
                null,
                "Partage de documents confidentiels détecté",
                Category.DATA_LEAK,
                Priority.CRITICAL,
                Status.OPEN,
                2);
    }

    User user(String name, String email, Role role) {
        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setRole(role);
        u.setPassword(encoder.encode("Demo2026!"));
        return users.save(u);
    }

    void ticket(
            User creator,
            User tech,
            String title,
            Category category,
            Priority priority,
            Status status,
            int hours) {
        Incident i = new Incident();
        i.setCreatedBy(creator);
        i.setAssignedTo(tech);
        i.setTitle(title);
        i.setCategory(category);
        i.setPriority(priority);
        i.setDescription(
                "Incident signalé par "
                        + creator.getFullName()
                        + ". "
                        + title
                        + ". Merci de vérifier la situation et de prendre les mesures"
                        + " nécessaires.");
        i.setCreatedAt(Instant.now().minusSeconds(hours * 3600L));
        i.setUpdatedAt(i.getCreatedAt());
        i.setStatus(status);
        event(i, creator, "Incident créé", i.getCreatedAt());
        if (tech != null) {
            event(i, tech, "Affecté à " + tech.getFullName(), i.getCreatedAt().plusSeconds(600));
        }
        if (status == Status.IN_PROGRESS || status == Status.RESOLVED || status == Status.CLOSED)
            event(i, tech, "ASSIGNED → IN_PROGRESS", i.getCreatedAt().plusSeconds(1200));
        if (status == Status.RESOLVED || status == Status.CLOSED)
            event(i, tech, "IN_PROGRESS → RESOLVED", i.getCreatedAt().plusSeconds(2400));
        if (status == Status.CLOSED)
            event(i, tech, "RESOLVED → CLOSED", i.getCreatedAt().plusSeconds(3600));
        if (status == Status.IN_PROGRESS) {
            IncidentComment c = new IncidentComment();
            c.setIncident(i);
            c.setAuthor(tech);
            c.setContent("Le poste a été isolé du réseau. Une analyse antivirus est en cours.");
            c.setCreatedAt(i.getCreatedAt().plusSeconds(1800));
            i.addComment(c);
            event(i, tech, "Note de traitement ajoutée", c.getCreatedAt());
        }
        incidents.save(i);
    }

    void event(Incident i, User u, String message, Instant at) {
        IncidentHistory h = new IncidentHistory();
        h.setIncident(i);
        h.setAuthor(u);
        h.setMessage(message);
        h.setCreatedAt(at);
        i.addHistory(h);
        i.setUpdatedAt(at);
    }
}
