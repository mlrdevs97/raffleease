package com.raffleease.raffleease.Domains.Tickets.Mappers;

import com.raffleease.raffleease.Domains.Tickets.DTO.TicketDTO;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TicketsMapper {

    public TicketDTO fromTicket(Ticket ticket) {
        return TicketDTO.builder()
                .id(ticket.getId())
                .raffleId(ticket.getRaffle().getId())
                .ticketNumber(ticket.getTicketNumber())
                .price(ticket.getPrice())
                .status(ticket.getStatus())
                .reservationFlag(ticket.getReservationFlag())
                .reservationTime(ticket.getReservationTime())
                .customerId(ticket.getCustomerId())
                .build();
    }

    public Set<TicketDTO> fromTicketSet(Set<Ticket> tickets) {
        return tickets.stream()
                .map(this::fromTicket)
                .collect(Collectors.toSet());
    }

    public List<TicketDTO> fromTicketList(List<Ticket> tickets) {
        return tickets.stream()
                .map(this::fromTicket)
                .collect(Collectors.toList());
    }
}