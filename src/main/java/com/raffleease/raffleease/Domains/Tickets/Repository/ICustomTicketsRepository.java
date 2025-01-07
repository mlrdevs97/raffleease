package com.raffleease.raffleease.Domains.Tickets.Repository;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus;

import java.util.List;

public interface ICustomTicketsRepository {
    List<Ticket> edit(List<Ticket> tickets, TicketStatus status);

    List<Ticket> findByTicketNumber(
            Raffle raffle,
            TicketStatus status,
            String ticketNumber
    );
}
