package com.minijira.seed;

import com.minijira.model.Project;
import com.minijira.model.Resource;
import com.minijira.model.Ticket;
import com.minijira.model.User;
import com.minijira.repository.ProjectRepository;
import com.minijira.repository.ResourceRepository;
import com.minijira.repository.TicketRepository;
import com.minijira.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Loads the same data you already had in db.json so the frontend keeps
 * working unchanged. Only runs once: if the projects table already has
 * rows, seeding is skipped.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final ProjectRepository projectRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    public DataSeeder(ProjectRepository projectRepository,
                       ResourceRepository resourceRepository,
                       UserRepository userRepository,
                       TicketRepository ticketRepository) {
        this.projectRepository = projectRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public void run(String... args) {
        if (projectRepository.count() > 0) {
            return; // already seeded
        }

        projectRepository.saveAll(List.of(
                new Project("p1", "MJ", "Mini Jira"),
                new Project("p2", "API", "API Platform"),
                new Project("ceJaoUAjk1I", "LAN", "LANTERN"),
                new Project("Ce-WiHiwT1Y", "GEN", "GENOFC")
        ));

        resourceRepository.saveAll(List.of(
                new Resource("e1n7DJ8ThpI", "RBL", "Team member"),
                new Resource("LcyJqvGn-1g", "ENZO", "Team member"),
                new Resource("hCLaVXGjyEk", "ED", "Team member"),
                new Resource("cuDXGsCdXmk", "JES", "Team member")
        ));

        userRepository.saveAll(List.of(
                new User("1", "admin", "admin"),
                new User("2", "dev", "dev")
        ));

        ticketRepository.saveAll(List.of(
                new Ticket("t1", "p1", 1, "Set up project skeleton",
                        "Bootstrap repo, linting, CI.", "done", "medium", "r1782653437166"),
                new Ticket("t2", "p1", 2, "Design ticket board layout",
                        "Kanban columns with stub-style cards.", "done", "high", "r1782653437166"),
                new Ticket("t3", "p1", 3, "Wire up drag and drop",
                        "Move tickets between columns.", "done", "urgent", "r1782653437166"),
                new Ticket("t4", "p1", 4, "Add search and filtering",
                        "Filter board by title or assignee.", "done", "medium", "r1782653437166"),
                new Ticket("t5", "p2", 1, "Define auth endpoints",
                        "Spec login, refresh, logout.", "backlog", "high", "r1782653617709"),
                new Ticket("t6", "p2", 2, "Rate limiting middleware",
                        "Per-key throttling.", "backlog", "medium", "r1782653622637"),
                new Ticket("qGj5eSBRKl0", "ceJaoUAjk1I", 1, "Deployment Plan (spike)",
                        "Create a deployment plan in VM", "backlog", "medium", "hCLaVXGjyEk")
        ));
    }
}
