package com.raffleease.raffleease.Domains.Tickets.Services.Impls;

import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Repository.ITicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsQueryService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class TicketsQueryServiceImpl implements ITicketsQueryService {
    private final ITicketsRepository repository;
    public List<Ticket> findAllById(Set<Long> ticketsIds) {
        try {
            return repository.findAllById(ticketsIds);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while retrieving tickets: " + ex.getMessage());
        }
    }
}
