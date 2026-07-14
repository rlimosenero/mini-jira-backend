package com.minijira.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_history")
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String ticketId;

    /**
     * The JSON field key that changed on the Ticket (e.g. "status", "resourceId").
     * Use "created" for the initial creation entry.
     */
    @Column(nullable = false)
    private String field;

    private String oldValue; // Human-readable display string, or null
    private String newValue; // Human-readable display string, or null

    @Column(nullable = false)
    private String changedBy; // username — use "system" until JWT is wired up

    @Column(nullable = false)
    private LocalDateTime changedAt;

    // ── Constructors ──────────────────────────────────────────────────────────

    protected TicketHistory() {}

    public TicketHistory(String ticketId, String field,
                         String oldValue, String newValue, String changedBy) {
        this.ticketId  = ticketId;
        this.field     = field;
        this.oldValue  = oldValue;
        this.newValue  = newValue;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }

    // ── Getters (add setters / Lombok as you prefer) ──────────────────────────

    public String getId()          { return id; }
    public String getTicketId()    { return ticketId; }
    public String getField()       { return field; }
    public String getOldValue()    { return oldValue; }
    public String getNewValue()    { return newValue; }
    public String getChangedBy()   { return changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }
}