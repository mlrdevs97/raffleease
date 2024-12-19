package com.raffleease.raffleease.Domains.Tickets.Services;

import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import java.util.Set;

public interface ITicketsCommandService {
    Set<Ticket> saveAll(Set<Ticket> tickets);
}
