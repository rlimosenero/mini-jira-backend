package com.minijira.controller;

import com.minijira.model.Sprint;
import com.minijira.repository.SprintRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sprints")
public class SprintController {

    private final SprintRepository sprintRepository;

    public SprintController(SprintRepository sprintRepository) {
        this.sprintRepository = sprintRepository;
    }

    /**
     * GET /sprints              -> all sprints
     * GET /sprints?projectId=p1 -> sprints for one project
     */
    @GetMapping
    public List<Sprint> getAll(@RequestParam(required = false) String projectId) {
        if (projectId != null) {
            return sprintRepository.findByProjectId(projectId);
        }
        return sprintRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sprint> getById(@PathVariable String id) {
        return sprintRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<Sprint> create(@Valid @RequestBody Sprint sprint) {
        if (sprint.getId() == null || sprint.getId().isBlank()) {
            sprint.setId(UUID.randomUUID().toString());
        }
        Sprint saved = sprintRepository.save(sprint);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<Sprint> update(@PathVariable String id, @Valid @RequestBody Sprint sprint) {
        if (!sprintRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        sprint.setId(id);
        return ResponseEntity.ok(sprintRepository.save(sprint));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!sprintRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        sprintRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
