package com.raffleease.raffleease.Domains.Tickets.Services;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketDTO;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus;

import java.util.List;

public interface ITicketsQueryService {
    List<Ticket> findAllById(List<Long> ticketsIds);
    List<TicketDTO> findByTicketNumber(Long raffleId, String ticketNumber);
    List<Ticket> findByRaffleAndStatus(Raffle raffle, TicketStatus status);
}
