package com.minijira.controller;

import com.minijira.model.CreateTicketCommentRequest;
import com.minijira.model.Ticket;
import com.minijira.model.TicketComment;
import com.minijira.model.TicketCommentResponse;
import com.minijira.repository.TicketCommentRepository;
import com.minijira.repository.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tickets/{ticketId}/comments")
public class TicketCommentController {
    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;

    public TicketCommentController(
            TicketRepository ticketRepository,
            TicketCommentRepository commentRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
    }

    @GetMapping
    public List<TicketCommentResponse> list(@PathVariable String ticketId) {
        return commentRepository.findByTicketIdOrderByCreatedAtDesc(ticketId)
                .stream()
                .map(c -> new TicketCommentResponse(
                        c.getId(),
                        c.getTicket().getId(),
                        c.getAuthor(),
                        c.getBody(),
                        c.getCreatedAt()
                ))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'DEVELOPER', 'VIEWER')")
    public TicketCommentResponse create(
            @PathVariable String ticketId,
            @RequestBody CreateTicketCommentRequest request
    ) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setAuthor(request.author());
        comment.setBody(request.body());
        comment.setCreatedAt(
                request.createdAt() != null ? request.createdAt() : LocalDateTime.now()
        );

        TicketComment saved = commentRepository.save(comment);

        return new TicketCommentResponse(
                saved.getId(),
                saved.getTicket().getId(),
                saved.getAuthor(),
                saved.getBody(),
                saved.getCreatedAt()
        );
    }
}