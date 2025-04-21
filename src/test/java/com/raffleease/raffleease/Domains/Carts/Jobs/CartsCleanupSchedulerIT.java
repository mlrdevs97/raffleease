package com.raffleease.raffleease.Domains.Carts.Jobs;

import com.raffleease.raffleease.Base.BaseSharedIT;
import com.raffleease.raffleease.Domains.Carts.Controllers.BaseAdminCartsIT;
import com.raffleease.raffleease.Domains.Carts.DTO.ReservationRequest;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Slf4j
public class CartsCleanupSchedulerIT extends BaseAdminCartsIT {
    @Autowired
    private CartsCleanupScheduler cleanupScheduler;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    void shouldReleaseTicketsAndClearExpiredCarts() throws Exception {
        // 1. Setup raffle and ticket
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1).andReturn());
        Long raffleId = createRaffle(images, associationId, accessToken);
        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        Ticket ticket = ticketsRepository.findAllByRaffle(raffle).get(0);

        // 2. Create cart and reserve ticket
        Long cartId = createCart(associationId, accessToken);
        Long reservedTicketId = tickets.get(0).getId();
        ReservationRequest request = ReservationRequest.builder().ticketsIds(List.of(reservedTicketId)).build();
        performReserveRequest(request, associationId, cartId, accessToken).andReturn();

        entityManager.createQuery("UPDATE Cart c SET c.updatedAt = :updatedAt WHERE c.id = :id")
                .setParameter("updatedAt", LocalDateTime.now().minusSeconds(3600))
                .setParameter("id", cartId)
                .executeUpdate();
        entityManager.clear();

        // 3. Run the cleanup job
        cleanupScheduler.releaseScheduled();

        // 4. Refresh and assert
        Cart refreshedCart = cartsRepository.findById(cartId).orElseThrow();
        assertThat(refreshedCart.getTickets()).isEmpty();

        Ticket refreshedTicket = ticketsRepository.findById(ticket.getId()).orElseThrow();
        assertThat(refreshedTicket.getStatus()).isEqualTo(AVAILABLE);

        // 5. check tickets availability in raffle
        assertThat(rafflesRepository.findById(raffleId).orElseThrow().getAvailableTickets())
                .isEqualTo(raffle.getAvailableTickets() + 1);
    }
}