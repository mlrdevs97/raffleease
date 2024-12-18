package com.raffleease.raffleease.Domains.Tickets.Services.Impls;

import com.raffleease.raffleease.Domains.Tickets.DTO.TicketsCreate;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Repository.ITicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsCreateService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;

@RequiredArgsConstructor
@Service
public class TicketsCreateServiceImpl implements ITicketsCreateService {
    private final ITicketsRepository repository;

    public Set<Ticket> createTickets(TicketsCreate request) {
        long upperLimit = request.lowerLimit() + request.amount() - 1;

        Set<Ticket> tickets = LongStream.rangeClosed(request.lowerLimit(), upperLimit)
                .mapToObj(i -> Ticket.builder()
                        .status(AVAILABLE)
                        .price(request.price())
                        .ticketNumber(Long.toString(i))
                        .build()
                ).collect(Collectors.toSet());

        return saveAll(tickets);
    }

    private Set<Ticket> saveAll(Set<Ticket> tickets) {
        try {
            return new HashSet<>(repository.saveAll(tickets));
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving tickets: " + ex.getMessage());

        }
    }
}
