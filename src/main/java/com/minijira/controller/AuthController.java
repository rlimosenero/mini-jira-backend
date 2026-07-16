package com.minijira.controller;

import com.minijira.config.JwtUtil;
import com.minijira.model.AuthRequest;
import com.minijira.model.AuthResponse;
import com.minijira.model.User;
import com.minijira.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        Optional<User> userOpt = userRepository.findByUsername(authRequest.getUsername());

        if (userOpt.isEmpty() || !passwordEncoder.matches(authRequest.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userOpt.get();
        String token = jwtUtil.generateToken(String.valueOf(user.getId()), user.getUsername(), user.getRole().name());

        AuthResponse response = new AuthResponse(
                token,
                user.getId().toString(),
                user.getUsername(),
                user.getRole().name()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest authRequest) {
        if (userRepository.findByUsername(authRequest.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = new User();
        user.setUsername(authRequest.getUsername());
        user.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        user.setRole(com.minijira.model.Role.VIEWER);

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(String.valueOf(savedUser.getId()), savedUser.getUsername(), savedUser.getRole().name());

        AuthResponse response = new AuthResponse(
                token,
                savedUser.getId().toString(),
                savedUser.getUsername(),
                savedUser.getRole().name()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
