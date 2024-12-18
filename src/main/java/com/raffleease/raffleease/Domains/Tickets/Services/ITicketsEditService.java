package com.raffleease.raffleease.Domains.Tickets.Services;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;

import java.util.Set;

public interface ITicketsEditService {
    void setRaffle(Raffle raffle, Set<Ticket> tickets);
}
