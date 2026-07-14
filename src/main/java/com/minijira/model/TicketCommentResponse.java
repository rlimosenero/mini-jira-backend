package com.minijira.model;

import java.time.LocalDateTime;

public record TicketCommentResponse(
        String id,
        String ticketId,
        String author,
        String body,
        LocalDateTime createdAt
) {}
