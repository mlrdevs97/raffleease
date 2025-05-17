package com.raffleease.raffleease.Domains.Carts.Controllers;

import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Carts.DTO.ReservationRequest;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.RESERVED;
import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.SOLD;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminCartsControllerReserveIT extends BaseAdminCartsIT {
    Long cartId;

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
        cartId = createCart(associationId, accessToken);
    }

    @Test
    void shouldReserveTickets() throws Exception {
        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(List.of(ticketId))
                .build();

        performReserveRequest(request, associationId, cartId, accessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("New reservation generated successfully"));

        Cart cart = cartsRepository.findById(cartId).orElseThrow();
        List<Ticket> tickets = ticketsRepository.findAllByCart(cart);
        Ticket ticket = tickets.get(0);

        assertThat(tickets.size()).isEqualTo(1);
        assertThat(ticket.getId()).isEqualTo(ticketId);
        assertThat(ticket.getStatus()).isEqualTo(RESERVED);
        assertThat(ticket.getRaffle().getAvailableTickets()).isEqualTo(raffle.getAvailableTickets() - 1);
    }

    @Test
    void shouldFailReserveIfTicketsDoNotBelongToAssociationRaffle() throws Exception {
        AuthResponse authResponse = registerOtherUser();
        String otherToken = authResponse.accessToken();
        Long associationId = authResponse.associationId();
        Long cartId = createCart(associationId, otherToken);

        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(List.of(tickets.get(0).getId()))
                .build();

        performReserveRequest(request, associationId, cartId, otherToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Some tickets do not belong to an association raffle"));
    }

    @Test
    void shouldFailReserveIfUserDoesNotBelongToAssociation() throws Exception {
        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(List.of(tickets.get(0).getId()))
                .build();

        String otherToken = registerOtherUser().accessToken();

        performReserveRequest(request, associationId, cartId, otherToken)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not a member of this association"));
    }

    @Test
    void shouldFailToReduceIfNotEnoughTicketsAvailable() throws Exception {
        raffle.setAvailableTickets(0L);
        raffle = rafflesRepository.save(raffle);

        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(List.of(tickets.get(0).getId()))
                .build();

        Long cartId = createCart(associationId, accessToken);

        performReserveRequest(request, associationId, cartId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient tickets available to complete the operation"));
    }

    @Test
    void shouldFailReserveIfTicketsDoNotExist() throws Exception {
        Long cartId = createCart(associationId, accessToken);

        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(List.of(999L))
                .build();

        performReserveRequest(request, associationId, cartId, accessToken)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Some tickets were not found"));
    }

    @Test
    void shouldFailReserveIfTicketsListIsNull() throws Exception {
        Long cartId = createCart(associationId, accessToken);

        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(null)
                .build();

        performReserveRequest(request, associationId, cartId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.ticketsIds").value("REQUIRED"));
    }

    @Test
    void shouldFailReserveIfTicketsListIsEmpty() throws Exception {
        Long cartId = createCart(associationId, accessToken);

        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(Collections.emptyList())
                .build();

        performReserveRequest(request, associationId, cartId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.ticketsIds").value("REQUIRED"));
    }

    @Test
    void shouldFailReserveIfTicketsAreNotAvailable() throws Exception {
        Ticket ticket = tickets.get(0);
        ticket.setStatus(SOLD);
        ticketsRepository.save(ticket);

        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(List.of(ticket.getId()))
                .build();

        performReserveRequest(request, associationId, cartId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Some tickets are not available"));
    }

    @Test
    void shouldFailIfNotEnoughTicketsAvailable() throws Exception {
        raffle.setAvailableTickets(0L);
        rafflesRepository.save(raffle);

        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(List.of(tickets.get(0).getId()))
                .build();

        performReserveRequest(request, associationId, cartId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient tickets available to complete the operation"));
    }
}