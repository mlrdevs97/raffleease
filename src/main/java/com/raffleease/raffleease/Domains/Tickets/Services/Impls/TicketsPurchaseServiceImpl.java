package com.raffleease.raffleease.Domains.Tickets.Services.Impls;

import com.raffleease.raffleease.Domains.Tickets.DTO.TicketDTO;
import com.raffleease.raffleease.Domains.Tickets.Mappers.TicketsMapper;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsCommandService;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsPurchaseService;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.SOLD;

@RequiredArgsConstructor
@Service
public class TicketsPurchaseServiceImpl implements ITicketsPurchaseService {
    private final ITicketsQueryService ticketsQueryService;
    private final ITicketsCommandService ticketsCommandService;
    private final TicketsMapper ticketsMapper;

    @Override
    public Set<TicketDTO> purchase(Set<Long> ticketIds, String customerId) {
        List<Ticket> tickets = ticketsQueryService.findAllById(ticketIds);
        Set<Ticket> ticketsToSell = tickets.stream().peek(ticket -> {
            ticket.setStatus(SOLD);
            ticket.setReservationFlag(null);
            ticket.setReservationTime(null);
            ticket.setCustomerId(customerId);
        }).collect(Collectors.toSet());
        Set<Ticket> savedTickets = ticketsCommandService.saveAll(ticketsToSell);
        return ticketsMapper.fromTicketSet(savedTickets);
    }
}
