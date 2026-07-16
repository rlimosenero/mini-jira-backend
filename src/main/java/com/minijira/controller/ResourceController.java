package com.minijira.controller;

import com.minijira.model.Resource;
import com.minijira.repository.ResourceRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceRepository resourceRepository;

    public ResourceController(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @GetMapping
    public List<Resource> getAll() {
        return resourceRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getById(@PathVariable String id) {
        return resourceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<Resource> create(@Valid @RequestBody Resource resource) {
        if (resource.getId() == null || resource.getId().isBlank()) {
            resource.setId(UUID.randomUUID().toString());
        }
        Resource saved = resourceRepository.save(resource);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<Resource> update(@PathVariable String id, @Valid @RequestBody Resource resource) {
        if (!resourceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        resource.setId(id);
        return ResponseEntity.ok(resourceRepository.save(resource));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!resourceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        resourceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
