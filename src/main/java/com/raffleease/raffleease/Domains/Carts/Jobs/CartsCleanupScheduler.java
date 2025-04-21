package com.raffleease.raffleease.Domains.Carts.Jobs;

import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.Repository.CartsRepository;
import com.raffleease.raffleease.Domains.Carts.Services.CartsService;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.RaffleTicketsAvailabilityService;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Services.TicketsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;

@RequiredArgsConstructor
@Service
public class CartsCleanupScheduler {
    private final CartsService cartsService;
    private final CartsRepository cartsRepository;
    private final TicketsService ticketsService;
    private final RaffleTicketsAvailabilityService raffleTicketsAvailabilityService;

    @Value("${spring.application.configs.cleanup.carts_cleanup_cutoff_seconds}")
    private Long cutoffSeconds;

    @Scheduled(cron = "${spring.application.configs.cron.carts_cleanup}")
    public void releaseScheduled() {
        LocalDateTime updatedAt = LocalDateTime.now().minusSeconds(cutoffSeconds);
        List<Cart> expiredCarts = cartsRepository.findAllByUpdatedAtBefore(updatedAt);
        for (Cart cart : expiredCarts) {
            releaseCart(cart);
        }
    }

    private void releaseCart(Cart cart) {
        List<Ticket> tickets = cart.getTickets();
        ticketsService.updateStatus(tickets, AVAILABLE);
        updateRaffleAvailability(tickets);
        cart.getTickets().removeAll(tickets);
        cartsService.save(cart);
    }

    private void updateRaffleAvailability(List<Ticket> tickets) {
        Map<Raffle, Long> ticketsByRaffle = tickets.stream().collect(
                Collectors.groupingBy(Ticket::getRaffle, Collectors.counting())
        );
        ticketsByRaffle.forEach(raffleTicketsAvailabilityService::increaseAvailableTickets);
    }
}
