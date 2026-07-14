package com.minijira.repository;

import com.minijira.model.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, String> {

    /** Returns history for one ticket, newest-first. */
    List<TicketHistory> findByTicketIdOrderByChangedAtDesc(String ticketId);
}
