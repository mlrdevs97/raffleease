package com.raffleease.raffleease.Domains.Carts.Jobs;

import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.Repository.CartsRepository;
import com.raffleease.raffleease.Domains.Carts.Services.ReservationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartsCleanupScheduler {
    private final CartsRepository cartsRepository;
    private final ReservationsService reservationsService;

    @Value("${spring.application.configs.cleanup.carts_cleanup_cutoff_seconds}")
    private Long cutoffSeconds;

    @Scheduled(cron = "${spring.application.configs.cron.carts_cleanup}")
    public void releaseScheduled() {
        LocalDateTime updatedAt = LocalDateTime.now().minusSeconds(cutoffSeconds);
        List<Cart> expiredCarts = cartsRepository.findAllByUpdatedAtBefore(updatedAt);
        for (Cart cart : expiredCarts) {
            reservationsService.release(cart);
            log.info("RELEASED CART: " + cart.getId());
            log.info("RELEASED CART TICKETS: " + cart.getTickets());
            if (cart.getTickets() != null && !cart.getTickets().isEmpty()) {
                log.info("RELEASED TICKET CART: " + cart.getTickets().get(0).getId());
            }
        }
    }
}
