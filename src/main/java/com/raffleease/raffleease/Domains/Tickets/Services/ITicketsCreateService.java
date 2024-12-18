package com.raffleease.raffleease.Domains.Tickets.Services;

import com.raffleease.raffleease.Domains.Tickets.DTO.TicketsCreate;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;

import java.util.Set;

public interface ITicketsCreateService {
    Set<Ticket> createTickets(TicketsCreate request);
}
