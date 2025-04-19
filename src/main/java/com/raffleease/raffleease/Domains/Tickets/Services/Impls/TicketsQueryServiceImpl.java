package com.raffleease.raffleease.Domains.Tickets.Services.Impls;

import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Customers.Services.CustomersService;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesPersistenceService;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketDTO;
import com.raffleease.raffleease.Domains.Tickets.Mappers.TicketsMapper;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus;
import com.raffleease.raffleease.Domains.Tickets.Repository.CustomTicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Repository.TicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Services.TicketsQueryService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.BusinessException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;

@RequiredArgsConstructor
@Service
public class TicketsQueryServiceImpl implements TicketsQueryService {
    private final RafflesPersistenceService rafflePersistence;
    private final TicketsRepository repository;
    private final CustomTicketsRepository customRepository;
    private final TicketsMapper mapper;
    private final CustomersService customersService;

    @Override
    public List<Ticket> findAllById(List<Long> ticketIds) {
        List<Ticket> tickets = repository.findAllById(ticketIds);
        if (tickets.isEmpty() || tickets.size() < ticketIds.size()) {
            throw new NotFoundException("Some tickets were not found");
        }
        return tickets;
    }

    @Override
    public List<TicketDTO> get(Long raffleId, String ticketNumber, TicketStatus status, Long customerId) {
        Raffle raffle = rafflePersistence.findById(raffleId);

        Customer customer = null;
        if (Objects.nonNull(customerId)) {
            customer = customersService.findById(customerId);
        }

        try {
            List<Ticket> searchResults = customRepository.search(raffle, ticketNumber, status, customer);

            if (searchResults.isEmpty()) throw new NotFoundException("No ticket was found for search");

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

    @Override
    public List<TicketDTO> getRandom(Long raffleId, Long quantity) {
        Raffle raffle = rafflePersistence.findById(raffleId);
        List<Ticket> availableTickets = findByRaffleAndStatus(raffle, AVAILABLE);
        validateTicketAvailability(availableTickets, quantity);
        List<Ticket> selectedTickets = selectRandomTickets(availableTickets, quantity);
        return mapper.fromTicketList(selectedTickets);
    }

    private void validateTicketAvailability(List<Ticket> availableTickets, Long requestedQuantity) {
        if (availableTickets.isEmpty() || availableTickets.size() < requestedQuantity) {
            throw new BusinessException("Not enough tickets were found for this order");
        }
    }

    private List<Ticket> selectRandomTickets(List<Ticket> availableTickets, Long quantity) {
        Collections.shuffle(availableTickets);
        return availableTickets.subList(0, quantity.intValue());
    }
}
