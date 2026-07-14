package com.minijira.repository;

import com.minijira.model.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketCommentRepository extends JpaRepository<TicketComment, String> {
    List<TicketComment> findByTicketIdOrderByCreatedAtDesc(String ticketId);
}