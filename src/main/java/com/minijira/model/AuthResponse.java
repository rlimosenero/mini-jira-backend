package com.minijira.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String userId;
    private String username;
    private String role;

}
