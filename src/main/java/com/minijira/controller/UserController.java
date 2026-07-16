package com.minijira.controller;

import com.minijira.model.Role;
import com.minijira.model.User;
import com.minijira.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Mirrors the json-server trick you were using for "login":
     * GET /users?username=admin&password=admin
     * Returns a list (empty if no match) just like json-server did, so your
     * existing Angular auth service call doesn't need to change.
     * <p>
     * NOTE: this sends the password back in the response and has no real
     * session/token. Fine for a prototype; swap for Spring Security + JWT
     * before this touches anything real.
     */
    @GetMapping
    public List<User> getAll(@RequestParam(required = false) String username,
                             @RequestParam(required = false) String password) {
        if (username != null && password != null) {
            return userRepository.findByUsernameAndPassword(username, password);
        }
        if (username != null) {
            return userRepository.findByUsername(username).map(List::of).orElseGet(List::of);
        }
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable String id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        if (user.getId() == null ) {
            user.setId(UUID.fromString(UUID.randomUUID().toString()));
        }
        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable String id, @Valid @RequestBody User user) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        user.setId(UUID.fromString(id));
        return ResponseEntity.ok(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        try {
            Role roleEnum = Role.valueOf(role.toUpperCase());
            List<User> users = userRepository.findAll()
                    .stream()
                    .filter(u -> u.getRole() == roleEnum)
                    .toList();
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}