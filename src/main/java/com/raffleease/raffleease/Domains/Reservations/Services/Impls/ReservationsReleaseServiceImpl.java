package com.raffleease.raffleease.Domains.Reservations.Services.Impls;

import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.Repository.ICustomCartRepository;
import com.raffleease.raffleease.Domains.Carts.Services.ICartsService;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.RaffleTicketsAvailabilityService;
import com.raffleease.raffleease.Domains.Reservations.DTOs.ReleaseRequest;
import com.raffleease.raffleease.Domains.Reservations.Services.IReservationsReleaseService;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsQueryService;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;

@RequiredArgsConstructor
@Service
public class ReservationsReleaseServiceImpl implements IReservationsReleaseService {
    private final ITicketsQueryService ticketsQueryService;
    private final ITicketsService ticketsService;
    private final RaffleTicketsAvailabilityService raffleTicketsAvailabilityService;
    private final ICartsService cartsService;
    private final ICustomCartRepository customCartRepository;

    @Override
    @Transactional
    public void release(Cart cart) {
        releaseInternal(cart, cart.getTickets());
    }

    @Override
    @Transactional
    public void release(ReleaseRequest request, Long cartId) {
        Cart cart = cartsService.findById(cartId);
        List<Ticket> tickets = ticketsQueryService.findAllById(request.ticketIds());
        releaseInternal(cart, tickets);
    }

    private void releaseInternal(Cart cart, List<Ticket> tickets) {
        ticketsService.edit(tickets, AVAILABLE);
        raffleTicketsAvailabilityService.increaseAvailableTickets(cart.getRaffle(), tickets.size());
        cartsService.removeTickets(cart, tickets);
    }

    @Scheduled(fixedRate = 1800000)
    @Transactional
    public void releaseScheduled() {
        LocalDateTime lastModified = LocalDateTime.now().minusMinutes(30);
        List<Cart> expiredCarts = customCartRepository.findExpiredCarts(lastModified);

        Map<Raffle, List<Ticket>> ticketsByRaffle = expiredCarts.stream()
                .flatMap(cart -> cart.getTickets().stream()
                        .map(ticket -> new AbstractMap.SimpleEntry<>(cart.getRaffle(), ticket)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        for (Map.Entry<Raffle, List<Ticket>> entry : ticketsByRaffle.entrySet()) {
            Raffle raffle = entry.getKey();
            List<Ticket> tickets = entry.getValue();

            ticketsService.edit(tickets, AVAILABLE);
            raffleTicketsAvailabilityService.increaseAvailableTickets(raffle, tickets.size());
        }

        customCartRepository.updateExpiredCart(lastModified);
    }
}
