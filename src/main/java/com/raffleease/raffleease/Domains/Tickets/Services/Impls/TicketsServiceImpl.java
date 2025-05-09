package com.raffleease.raffleease.Domains.Tickets.Services.Impls;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketsCreate;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus;
import com.raffleease.raffleease.Domains.Tickets.Repository.CustomTicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Repository.TicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Services.TicketsService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;

@RequiredArgsConstructor
@Service
public class TicketsServiceImpl implements TicketsService {
    private final TicketsRepository repository;
    private final CustomTicketsRepository customRepository;

    @Override
    public List<Ticket> create(Raffle raffle, TicketsCreate request) {
        long upperLimit = request.lowerLimit() + request.amount() - 1;
        return LongStream.rangeClosed(request.lowerLimit(), upperLimit)
                .mapToObj(i -> Ticket.builder()
                        .status(AVAILABLE)
                        .ticketNumber(Long.toString(i))
                        .raffle(raffle)
                        .build()
                ).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Ticket> updateStatus(List<Ticket> tickets, TicketStatus status) {
        try {
            return customRepository.updateStatus(tickets, status);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while updating tickets status: " + ex.getMessage());
        }
    }

    @Override
    public void releaseFromCart(List<Ticket> tickets) {
        saveAll(tickets.stream().peek(ticket -> {
            ticket.setStatus(AVAILABLE);
            ticket.setCart(null);
        }).toList());
    }

    @Override
    public List<Ticket> saveAll(List<Ticket> entities) {
        try {
            return repository.saveAll(entities);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving entities: " + ex.getMessage());
        }
    }
}
