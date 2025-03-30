package com.raffleease.raffleease.Domains.Reservations.Services.Impls;

import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesPersistenceService;
import com.raffleease.raffleease.Domains.Reservations.Services.IReservationsRandomService;
import com.raffleease.raffleease.Domains.Reservations.Services.IReservationsCreateService;
import com.raffleease.raffleease.Domains.Reservations.DTOs.GenerateRandom;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsQueryService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;

@RequiredArgsConstructor
@Service
public class ReservationsRandomService implements IReservationsRandomService {
    private final IReservationsCreateService reservationService;
    private final ITicketsQueryService ticketsQueryService;
    private final RafflesPersistenceService rafflePersistence;

    @Override
    public CartDTO generateRandom(GenerateRandom request, String cartId) {
        Raffle raffle = rafflePersistence.findById(request.raffleId());
        List<Ticket> availableTickets = ticketsQueryService.findByRaffleAndStatus(raffle, AVAILABLE);
        validateTicketAvailability(availableTickets, request.quantity());
        List<Ticket> selectedTickets = selectRandomTickets(availableTickets, request.quantity());
        return reservationService.reserve(request.raffleId(), selectedTickets, cartId);
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
