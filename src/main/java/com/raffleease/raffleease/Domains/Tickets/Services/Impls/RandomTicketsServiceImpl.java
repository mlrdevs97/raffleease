package com.raffleease.raffleease.Domains.Tickets.Services.Impls;

import com.raffleease.raffleease.Domains.Tickets.DTO.GenerateRandom;
import com.raffleease.raffleease.Domains.Tickets.DTO.ReservationResponse;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Repository.ITicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Services.IRandomTicketsService;
import com.raffleease.raffleease.Domains.Tickets.Services.IReservationsService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.BusinessException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;

@RequiredArgsConstructor
@Service
public class RandomTicketsServiceImpl implements IRandomTicketsService {
    private final IReservationsService reservationService;
    private final ITicketsRepository repository;

    @Override
    public ReservationResponse generateRandom(GenerateRandom request) {
        Set<Ticket> availableTickets = findAvailableTickets(request.raffleId());
        validateTicketAvailability(availableTickets, request.quantity());
        Set<Ticket> selectedTickets = selectRandomTickets(availableTickets, request.quantity());
        return reserveTickets(request.raffleId(), selectedTickets);
    }

    private Set<Ticket> findAvailableTickets(Long raffleId) {
        try {
            return new HashSet<>(repository.findByRaffleIdAndStatus(raffleId, AVAILABLE));
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while retrieving tickets: " + ex.getMessage());
        }
    }

    private void validateTicketAvailability(Set<Ticket> availableTickets, Long requestedQuantity) {
        if (availableTickets.isEmpty() || availableTickets.size() < requestedQuantity) {
            throw new BusinessException("Not enough tickets were found for this order");
        }
    }

    private ReservationResponse reserveTickets(Long raffleId, Set<Ticket> tickets) {
        return reservationService.reserve(raffleId, tickets);
    }

    private Set<Ticket> selectRandomTickets(Set<Ticket> availableTickets, Long quantity) {
        List<Ticket> tickets = new ArrayList<>(availableTickets);
        Collections.shuffle(tickets);
        tickets = tickets.subList(0, quantity.intValue());
        return new HashSet<>(tickets);
    }
}
