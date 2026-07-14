package com.minijira.repository;

import com.minijira.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, String> {

    List<Ticket> findByProjectId(String projectId);

    List<Ticket> findByProjectIdAndStatus(String projectId, String status);

    List<Ticket> findByResourceId(String resourceId);

    List<Ticket> findByStatus(String status);

    List<Ticket> findBySprintId(String sprintId);

    int countByProjectId(String projectId);

    @Query("SELECT MAX(t.num) FROM Ticket t WHERE t.projectId = :projectId")
    Optional<Integer> findMaxNumByProjectId(@Param("projectId") String projectId);
}
