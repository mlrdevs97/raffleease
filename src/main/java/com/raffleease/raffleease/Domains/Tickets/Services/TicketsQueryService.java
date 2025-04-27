package com.raffleease.raffleease.Domains.Tickets.Services;

import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketDTO;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketsSearchFilters;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TicketsQueryService {
    List<Ticket> findAllById(List<Long> ticketsIds);
    Page<TicketDTO> search(Long associationId, Long raffleId, TicketsSearchFilters searchFilters, Pageable pageable);
    List<Ticket> findByRaffleAndStatus(Raffle raffle, TicketStatus status);
    List<TicketDTO> getRandom(Long raffleId, Long quantity);
    List<Ticket> findAllByCart(Cart cart);
}
