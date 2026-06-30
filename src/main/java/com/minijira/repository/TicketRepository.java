package com.minijira.repository;

import com.minijira.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, String> {

    List<Ticket> findByProjectId(String projectId);

    List<Ticket> findByProjectIdAndStatus(String projectId, String status);

    List<Ticket> findByResourceId(String resourceId);

    List<Ticket> findByStatus(String status);

    List<Ticket> findBySprintId(String sprintId);

    int countByProjectId(String projectId);
}
