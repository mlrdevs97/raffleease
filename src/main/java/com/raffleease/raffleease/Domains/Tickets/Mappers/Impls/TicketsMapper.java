package com.raffleease.raffleease.Domains.Tickets.Mappers.Impls;

import com.raffleease.raffleease.Domains.Tickets.DTO.TicketDTO;
import com.raffleease.raffleease.Domains.Tickets.Mappers.ITicketsMapper;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TicketsMapper implements ITicketsMapper {
    public TicketDTO fromTicket(Ticket ticket) {
        return TicketDTO.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .status(ticket.getStatus())
                .build();
    }

    public List<TicketDTO> fromTicketList(List<Ticket> tickets) {
        return tickets.stream()
                .map(this::fromTicket)
                .collect(Collectors.toList());
    }
}