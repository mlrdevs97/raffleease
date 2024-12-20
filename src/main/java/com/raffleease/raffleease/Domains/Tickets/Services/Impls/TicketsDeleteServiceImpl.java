package com.raffleease.raffleease.Domains.Tickets.Services.Impls;

import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Repository.ITicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsDeleteService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class TicketsDeleteServiceImpl implements ITicketsDeleteService {
    private final ITicketsRepository ticketsRepository;

    @Override
    public void delete(Set<Ticket> tickets) {
        try {
            ticketsRepository.deleteAll(tickets);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while deleting tickets: " + ex.getMessage());
        }
    }
}
