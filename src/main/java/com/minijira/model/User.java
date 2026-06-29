package com.minijira.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    @NotBlank
    private String username;

    // Plaintext to match the existing json-server prototype data.
    // Swap this for a hashed password + real auth (e.g. Spring Security + JWT) before going further.
    @NotBlank
    private String password;
}
