package com.raffleease.raffleease.Domains.Reservations.Services.Impls;

import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Carts.Mappers.ICartsMapper;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.Services.ICartsService;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.RaffleTicketsAvailabilityService;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesPersistenceService;
import com.raffleease.raffleease.Domains.Reservations.DTOs.ReservationRequest;
import com.raffleease.raffleease.Domains.Reservations.Services.IReservationsCreateService;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsQueryService;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;
import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.RESERVED;

@RequiredArgsConstructor
@Service
public class ReservationsCreateServiceImpl implements IReservationsCreateService {
    private final RafflesPersistenceService rafflePersistence;
    private final ITicketsQueryService ticketsQueryService;
    private final RaffleTicketsAvailabilityService raffleTicketsAvailabilityService;
    private final ITicketsService ticketsService;
    private final ICartsService cartsService;
    private final ICartsMapper cartsMapper;

    @Override
    public CartDTO reserve(ReservationRequest request, String cartId) {
        List<Ticket> tickets = ticketsQueryService.findAllById(request.ticketsIds());
        checkTicketsAvailability(tickets);
        return reserveInternal(request.raffleId(), tickets, cartId);
    }

    @Override
    public CartDTO reserve(Long raffleId, List<Ticket> tickets, String cartId) {
        return reserveInternal(raffleId, tickets, cartId);
    }

    @Transactional
    private CartDTO reserveInternal(Long raffleId, List<Ticket> tickets, String cartId) {
        List<Ticket> updatedTickets = ticketsService.edit(tickets, RESERVED);
        raffleTicketsAvailabilityService.reduceAvailableTickets(raffleId, updatedTickets.size());
        Cart cart = createOrRetrieveCart(raffleId, tickets, cartId);
        return cartsMapper.fromCart(cart);
    }

    private Cart createOrRetrieveCart(Long raffleId, List<Ticket> tickets, String cartId) {
        if (Objects.nonNull(cartId)) {
            Cart cart = cartsService.findById(Long.parseLong(cartId));
            return cartsService.addTickets(cart, tickets);
        } else {
            Raffle raffle = rafflePersistence.findById(raffleId);
            return cartsService.create(raffle, tickets);
        }
    }

    private void checkTicketsAvailability(List<Ticket> tickets) {
        boolean anyUnavailable = tickets.stream()
                .anyMatch(ticket -> ticket.getStatus() != AVAILABLE);
        if (anyUnavailable) {
            throw new BusinessException("One or more tickets are not available");
        }
    }
}