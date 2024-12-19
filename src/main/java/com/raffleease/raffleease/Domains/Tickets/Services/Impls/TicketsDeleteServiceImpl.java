package com.raffleease.raffleease.Domains.Tickets.Services.Impls;

import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Repository.ITicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsDeleteService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import org.springframework.dao.DataAccessException;

import java.util.Set;

public class TicketsDeleteServiceImpl implements ITicketsDeleteService {
    private ITicketsRepository ticketsRepository;

    @Override
    public void delete(Set<Ticket> tickets) {
        try {
            ticketsRepository.deleteAll(tickets);
        } catch (DataAccessException exp) {
            throw new DatabaseException("Database error occurred while deleting tickets: " + exp.getMessage());
        }
    }
}
