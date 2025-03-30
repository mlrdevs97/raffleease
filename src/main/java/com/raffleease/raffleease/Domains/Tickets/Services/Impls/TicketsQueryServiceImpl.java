package com.raffleease.raffleease.Domains.Tickets.Services.Impls;

import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Customers.Services.ICustomersService;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesPersistenceService;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketDTO;
import com.raffleease.raffleease.Domains.Tickets.Mappers.ITicketsMapper;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus;
import com.raffleease.raffleease.Domains.Tickets.Repository.ICustomTicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Repository.TicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsQueryService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;

@RequiredArgsConstructor
@Service
public class TicketsQueryServiceImpl implements ITicketsQueryService {
    private final RafflesPersistenceService rafflePersistence;
    private final TicketsRepository repository;
    private final ICustomTicketsRepository customRepository;
    private final ITicketsMapper mapper;
    private final ICustomersService customersService;

    @Override
    public List<Ticket> findAllById(List<Long> ticketIds) {
        List<Ticket> tickets = repository.findAllById(ticketIds);
        if (tickets.isEmpty()) throw new NotFoundException("No tickets were found for provided ids");
        if (tickets.size() < ticketIds.size()) {
            throw new NotFoundException("One or more tickets could not be found");
        }
        return tickets;
    }

    @Override
    public List<TicketDTO> get(Long raffleId, String ticketNumber, TicketStatus status, Long customerId) {
        Raffle raffle = rafflePersistence.findById(raffleId);

        // TODO
        Customer customer = new Customer();

        try {
            List<Ticket> searchResults = customRepository.search(
                    raffle,
                    ticketNumber,
                    AVAILABLE,
                    customer
            );
            if (searchResults.isEmpty()) {
                throw new NotFoundException("No ticket for search was found");
            }
            return mapper.fromTicketList(searchResults);
        } catch (DataAccessException exp) {
            throw new DatabaseException("Database error occurred while retrieving tickets: " + exp.getMessage());
        }
    }

    @Override
    public List<Ticket> findByRaffleAndStatus(Raffle raffle, TicketStatus ticketStatus) {
        try {
            return repository.findByRaffleAndStatus(raffle, AVAILABLE);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while retrieving tickets: " + ex.getMessage());
        }
    }
}
