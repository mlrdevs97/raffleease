package com.raffleease.raffleease.Domains.Tickets.Services.Impls;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Repository.ICustomTicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Repository.ITicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsEditService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class TicketsEditServiceImpl implements ITicketsEditService {
    private final ICustomTicketsRepository repository;
    public void setRaffle(Raffle raffle, Set<Ticket> tickets) {
        try {
            repository.setRaffle(raffle, tickets);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while setting tickets raffle: " + ex.getMessage());
        }
    }
}
