package com.minijira.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Flat/denormalized like the original json-server "tickets" collection:
 * projectId and resourceId are plain string foreign keys rather than
 * JPA relationships, so the JSON shape your Angular app already expects
 * doesn't change. Swap to @ManyToOne later if you want referential
 * integrity enforced at the DB level.
 */
@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    private String id;

    @NotBlank
    @Column(name = "project_id")
    private String projectId;

    private int num;

    @NotBlank
    private String title;

    @Column(length = 2000)
    private String description;

    // backlog | in-progress | done | ... kept as a free string to match json-server flexibility
    @NotBlank
    private String status;

    // low | medium | high | urgent
    @NotBlank
    private String priority;

    @Column(name = "resource_id")
    private String resourceId;
}
