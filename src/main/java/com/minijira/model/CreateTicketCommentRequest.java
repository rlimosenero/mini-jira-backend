package com.minijira.model;

import java.time.LocalDateTime;

public record CreateTicketCommentRequest(
        String author,
        String body,
        LocalDateTime createdAt
) {}
