package com.raffleease.raffleease.Domains.Tickets.Repository;

import com.raffleease.raffleease.Domains.Tickets.DTO.TicketsSearchFilters;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomTicketsRepository {
    List<Ticket> updateStatus(List<Ticket> tickets, TicketStatus status);
    Page<Ticket> search(TicketsSearchFilters searchFilters, Long associationId, Long raffleId, Pageable pageable);
}
