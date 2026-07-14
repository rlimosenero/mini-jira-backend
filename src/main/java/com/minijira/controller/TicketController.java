package com.minijira.controller;

import com.minijira.model.Ticket;
import com.minijira.model.TicketHistory;
import com.minijira.repository.TicketHistoryRepository;
import com.minijira.repository.TicketRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;
    private final TicketHistoryRepository historyRepo ;

    public TicketController(TicketRepository ticketRepository, TicketHistoryRepository historyRepo) {
        this.ticketRepository = ticketRepository;
        this.historyRepo = historyRepo;
    }


    /**
     * Supports the same query-string filtering your Angular services already
     * call against json-server, e.g.:
     *   GET /tickets?projectId=p1
     *   GET /tickets?projectId=p1&status=done
     *   GET /tickets?resourceId=hCLaVXGjyEk
     *   GET /tickets?sprintId=s1
     */
    @GetMapping
    public List<Ticket> getAll(@RequestParam(required = false) String projectId,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String resourceId,
                                @RequestParam(required = false) String sprintId) {
        if (sprintId != null) {
            return ticketRepository.findBySprintId(sprintId);
        }
        if (projectId != null && status != null) {
            return ticketRepository.findByProjectIdAndStatus(projectId, status);
        }
        if (projectId != null) {
            return ticketRepository.findByProjectId(projectId);
        }
        if (resourceId != null) {
            return ticketRepository.findByResourceId(resourceId);
        }
        if (status != null) {
            return ticketRepository.findByStatus(status);
        }
        return ticketRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getById(@PathVariable String id) {
        return ticketRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Ticket> create(@Valid @RequestBody Ticket ticket) {
        if (ticket.getId() == null || ticket.getId().isBlank()) {
            ticket.setId(UUID.randomUUID().toString());
        }
        if (ticket.getNum() <= 0) {
            int count = ticketRepository.countByProjectId(ticket.getProjectId());
            ticket.setNum(count + 1);
        }
        Ticket saved = ticketRepository.save(ticket);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> update(@PathVariable String id, @Valid @RequestBody Ticket ticket) {
        if (!ticketRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ticket.setId(id);
        return ResponseEntity.ok(ticketRepository.save(ticket));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<TicketHistory>> getHistory(@PathVariable String id) {
        return ResponseEntity.ok(historyRepo.findByTicketIdOrderByChangedAtDesc(id));
    }

    /**
     * Partial update — handy for drag-and-drop, where you only want to send
     * the changed field (typically { "status": "in-progress" }) instead of
     * the whole ticket payload.
     *
     * completedAt is managed here rather than trusted from the client: it's
     * stamped with today's date the moment status transitions into "done",
     * and cleared if a ticket is moved back out of "done". This keeps
     * velocity reporting accurate even if a client forgets to set it.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Ticket> patch(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        return ticketRepository.findById(id).map(ticket -> {
            String previousStatus = ticket.getStatus();

            if (updates.containsKey("status")) {
                ticket.setStatus((String) updates.get("status"));
            }
            if (updates.containsKey("priority")) {
                ticket.setPriority((String) updates.get("priority"));
            }
            if (updates.containsKey("title")) {
                ticket.setTitle((String) updates.get("title"));
            }
            if (updates.containsKey("description")) {
                ticket.setDescription((String) updates.get("description"));
            }
            if (updates.containsKey("resourceId")) {
                ticket.setResourceId((String) updates.get("resourceId"));
            }
            if (updates.containsKey("projectId")) {
                ticket.setProjectId((String) updates.get("projectId"));
            }
            if (updates.containsKey("sprintId")) {
                ticket.setSprintId((String) updates.get("sprintId"));
            }
            if (updates.containsKey("storyPoints")) {
                Object sp = updates.get("storyPoints");
                ticket.setStoryPoints(sp == null ? null : ((Number) sp).intValue());
            }
            if (updates.containsKey("num")) {
                ticket.setNum(((Number) updates.get("num")).intValue());
            }

            boolean movedToDone = "done".equals(ticket.getStatus()) && !"done".equals(previousStatus);
            boolean movedOffDone = !"done".equals(ticket.getStatus()) && "done".equals(previousStatus);
            if (movedToDone) {
                ticket.setCompletedAt(java.time.LocalDate.now());
            } else if (movedOffDone) {
                ticket.setCompletedAt(null);
            }

            return ResponseEntity.ok(ticketRepository.save(ticket));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!ticketRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ticketRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
