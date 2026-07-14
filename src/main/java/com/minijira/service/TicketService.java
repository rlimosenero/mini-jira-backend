package com.minijira.service;

import com.minijira.model.*;
import com.minijira.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepo;
    private final TicketHistoryRepository historyRepo;
    private final ResourceRepository resourceRepo;
    private final SprintRepository sprintRepo;
    private final ProjectRepository projectRepo;

    public TicketService(TicketRepository ticketRepo, TicketHistoryRepository historyRepo, ResourceRepository resourceRepo, SprintRepository sprintRepo, ProjectRepository projectRepo) {
        this.ticketRepo = ticketRepo;
        this.historyRepo = historyRepo;
        this.resourceRepo = resourceRepo;
        this.sprintRepo = sprintRepo;
        this.projectRepo = projectRepo;
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public Ticket createTicket(Ticket ticket) {
        // Let Spring / Hibernate generate the UUID
        ticket.setCreatedAt(LocalDateTime.now());
        Ticket saved = ticketRepo.save(ticket);

        // Record the "created" sentinel entry
        historyRepo.save(new TicketHistory(
                saved.getId(), "created", null,
                saved.getTitle(),   // newValue = ticket title for context
                currentUser()
        ));

        return saved;
    }

    // ── Update (PATCH) ────────────────────────────────────────────────────────

    public Ticket patchTicket(String id, Map<String, Object> patch) {
        Ticket old = ticketRepo.findById(id).orElseThrow();
        List<TicketHistory> entries = new ArrayList<>();

        patch.forEach((field, rawValue) -> {
            switch (field) {
                case "status" -> {
                    String n = (String) rawValue;
                    if (!n.equals(old.getStatus())) {
                        entries.add(entry(id, "status",
                                labelStatus(old.getStatus()), labelStatus(n)));
                    }
                    old.setStatus(n);
                    if ("done".equals(n)) old.setCompletedAt(LocalDate.now());
                    else old.setCompletedAt(null);
                }
                case "priority" -> {
                    String n = (String) rawValue;
                    if (!n.equals(old.getPriority()))
                        entries.add(entry(id, "priority",
                                capitalize(old.getPriority()), capitalize(n)));
                    old.setPriority(n);
                }
                case "title" -> {
                    String n = (String) rawValue;
                    if (!n.equals(old.getTitle()))
                        entries.add(entry(id, "title", old.getTitle(), n));
                    old.setTitle(n);
                }
                case "description" -> {
                    // Only record that description changed, not the full text
                    // (keeps history readable)
                    if (!Objects.equals(rawValue, old.getDescription()))
                        entries.add(entry(id, "description", "…", "…"));
                    old.setDescription((String) rawValue);
                }
                case "storyPoints" -> {
                    Integer n = rawValue != null ? ((Number) rawValue).intValue() : null;
                    if (!Objects.equals(n, old.getStoryPoints()))
                        entries.add(entry(id, "storyPoints",
                                old.getStoryPoints() != null ? old.getStoryPoints().toString() : "—",
                                n != null ? n.toString() : "—"));
                    old.setStoryPoints(n);
                }
                case "resourceId" -> {
                    String n = (String) rawValue;
                    if (!Objects.equals(n, old.getResourceId()))
                        entries.add(entry(id, "resourceId",
                                resolveName(old.getResourceId()), resolveName(n)));
                    old.setResourceId(n);
                }
                case "sprintId" -> {
                    String n = (String) rawValue;
                    if (!Objects.equals(n, old.getSprintId()))
                        entries.add(entry(id, "sprintId",
                                resolveSprintName(old.getSprintId()),
                                resolveSprintName(n)));
                    old.setSprintId(n);
                }
                case "projectId" -> {
                    String n = (String) rawValue;
                    if (!Objects.equals(n, old.getProjectId()))
                        entries.add(entry(id, "projectId",
                                resolveProjectName(old.getProjectId()),
                                resolveProjectName(n)));
                    old.setProjectId(n);
                    int nextNum = ticketRepo.findMaxNumByProjectId(n).orElse(0) + 1;
                    old.setNum(nextNum); // recalculate ticket number
                    old.setSprintId(null);  // sprint belongs to old project
                }
            }
        });

        Ticket saved = ticketRepo.save(old);
        historyRepo.saveAll(entries);
        return saved;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private TicketHistory entry(String ticketId, String field,
                                String oldVal, String newVal) {
        return new TicketHistory(ticketId, field, oldVal, newVal, currentUser());
    }

    private String resolveName(String resourceId) {
        if (resourceId == null) return "Unassigned";
        return resourceRepo.findById(resourceId)
                .map(Resource::getName).orElse("Unknown");
    }

    private String resolveSprintName(String sprintId) {
        if (sprintId == null) return "No sprint";
        return sprintRepo.findById(sprintId)
                .map(Sprint::getName).orElse("Unknown sprint");
    }

    private String resolveProjectName(String projectId) {
        if (projectId == null) return "—";
        return projectRepo.findById(projectId)
                .map(Project::getName).orElse("Unknown project");
    }

    private static String labelStatus(String s) {
        return switch (s) {
            case "backlog"  -> "Backlog";
            case "progress" -> "In Progress";
            case "review"   -> "Review";
            case "done"     -> "Done";
            default         -> s;
        };
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Placeholder until JWT is wired up.
     * Replace with SecurityContextHolder.getContext().getAuthentication().getName()
     * when auth is ready.
     */
    private String currentUser() {
        return "system";
    }
}
