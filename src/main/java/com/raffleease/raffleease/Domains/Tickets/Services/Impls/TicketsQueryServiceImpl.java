package com.raffleease.raffleease.Domains.Tickets.Services.Impls;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesQueryService;
import com.raffleease.raffleease.Domains.Tickets.DTO.SearchRequest;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketDTO;
import com.raffleease.raffleease.Domains.Tickets.Mappers.TicketsMapper;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Repository.ICustomTicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Repository.ITicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsQueryService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;

@RequiredArgsConstructor
@Service
public class TicketsQueryServiceImpl implements ITicketsQueryService {
    private final IRafflesQueryService rafflesQueryService;
    private final ITicketsRepository repository;
    private final TicketsMapper mapper;

    @Override
    public List<Ticket> findAllById(Set<Long> ticketsIds) {
        try {
            return repository.findAllById(ticketsIds);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while retrieving tickets: " + ex.getMessage());
        }
    }

    @Override
    public List<TicketDTO> findByTicketNumber(SearchRequest request) {
        Set<Ticket> searchResults = searchTicketsByNumber(request.raffleId(), request.ticketNumber());
        List<Ticket> sortedResult = sortTicketsByNumber(searchResults);
        return mapper.fromTicketList(sortedResult);
    }

    private Set<Ticket> searchTicketsByNumber(Long raffleId, String ticketNumber) {
        Raffle raffle = rafflesQueryService.findById(raffleId);
        try {
            List<Ticket> searchResults = repository.findByRaffleAndStatusAndTicketNumberContaining(
                    raffle,
                    AVAILABLE,
                    ticketNumber
            );
            if (searchResults.isEmpty()) {
                throw new NotFoundException("No ticket for search was found");
            }
            return new HashSet<>(searchResults);
        } catch (Exception exp) {
            throw new DatabaseException("Failed to access database when searching tickets: " + exp.getMessage());
        }
    }

    private List<Ticket> sortTicketsByNumber(Set<Ticket> tickets) {
        return tickets.stream()
                .sorted(Comparator.comparing(ticket -> Long.parseLong(ticket.getTicketNumber())))
                .collect(Collectors.toList());
    }
}
