package com.raffleease.raffleease.Domains.Tickets.Services.Impls;

import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Repository.ITicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsCommandService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class TicketsCommandServiceImpl implements ITicketsCommandService {
    private final ITicketsRepository repository;

    @Override
    public Set<Ticket> saveAll(Set<Ticket> tickets) {
        try {
            return new HashSet<>(repository.saveAll(tickets));
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving tickets: " + ex.getMessage());
        }
    }
}
