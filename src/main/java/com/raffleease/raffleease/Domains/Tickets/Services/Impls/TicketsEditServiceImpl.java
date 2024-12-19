package com.raffleease.raffleease.Domains.Tickets.Services.Impls;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketDTO;
import com.raffleease.raffleease.Domains.Tickets.Mappers.TicketsMapper;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Repository.ICustomTicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Repository.ITicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsCommandService;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsEditService;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsQueryService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.SOLD;

@RequiredArgsConstructor
@Service
public class TicketsEditServiceImpl implements ITicketsEditService {
    private final ICustomTicketsRepository repository;

    @Override
    public void setRaffle(Raffle raffle, Set<Ticket> tickets) {
        try {
            repository.setRaffle(raffle, tickets);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while setting tickets raffle: " + ex.getMessage());
        }
    }
}
