package com.minijira.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minijira.model.Project;
import com.minijira.model.Resource;
import com.minijira.model.Ticket;
import com.minijira.model.User;
import com.minijira.repository.ProjectRepository;
import com.minijira.repository.ResourceRepository;
import com.minijira.repository.TicketRepository;
import com.minijira.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * Loads the same data you already had in db.json so the frontend keeps
 * working unchanged. Only runs once: if the projects table already has
 * rows, seeding is skipped.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProjectRepository projectRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    private final ObjectMapper objectMapper;

    @Value("${app.seed.enabled}")
    private boolean seedEnabled;

    @Value("${app.seed.path:}")   // External folder
    private String seedPath;

    @Override
    public void run(String... args) {
        if (!seedEnabled) return;

        if (projectRepository.count() > 0) {
            System.out.println("Database already seeded. Skipping...");
            return;
        }
        String base = seedPath.endsWith("/") ? seedPath : seedPath + "/";
        try {


            seedProjects(base + "projects.json");
            seedResources(base + "resources.json");
            seedUsers(base + "users.json");
            seedTickets(base + "tickets.json");

            System.out.println(base+"✅ Seed data loaded from external JSON files!");

        } catch (Exception e) {
            System.err.println(base+"❌ Seeding failed: " + e.getMessage());
            e.printStackTrace();   // This will show full details
        }
    }

    private void seedProjects(String filePath) throws Exception {
        List<Project> list = objectMapper.readValue(new File(filePath), new TypeReference<>() {});
        projectRepository.saveAll(list);
    }

    private void seedResources(String filePath) throws Exception {
        List<Resource> list = objectMapper.readValue(new File(filePath), new TypeReference<>() {});
        resourceRepository.saveAll(list);
    }

    private void seedUsers(String filePath) throws Exception {
        List<User> list = objectMapper.readValue(new File(filePath), new TypeReference<>() {});
        userRepository.saveAll(list);
    }

    private void seedTickets(String filePath) throws Exception {
        List<Ticket> list = objectMapper.readValue(new File(filePath), new TypeReference<>() {});
        ticketRepository.saveAll(list);
    }
}