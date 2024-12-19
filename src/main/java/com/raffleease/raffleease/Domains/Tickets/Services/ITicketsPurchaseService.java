package com.raffleease.raffleease.Domains.Tickets.Services;

import com.raffleease.raffleease.Domains.Tickets.DTO.TicketDTO;

import java.util.Set;

public interface ITicketsPurchaseService {
    Set<TicketDTO> purchase(Set<Long> ticketIds, String customerId);
}
