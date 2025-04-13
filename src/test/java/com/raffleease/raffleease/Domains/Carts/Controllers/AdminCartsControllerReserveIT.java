package com.raffleease.raffleease.Domains.Carts.Controllers;

import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Carts.DTO.ReservationRequest;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.RESERVED;
import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.SOLD;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminCartsControllerReserveIT extends BaseAdminCartsIT {
    @Test
    @Transactional
    void shouldReserveTickets() throws Exception {
        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        List<Ticket> tickets = raffle.getTickets();

        Long cartId = createCart(getCreateCartURL(), accessToken);
        Long ticketId = tickets.get(0).getId();

        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(List.of(ticketId))
                .build();

        performReserveRequest(request, getReserveURL(cartId), accessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("New reservation generated successfully"));

        Cart cart = cartsRepository.findById(cartId).orElseThrow();
        Ticket ticket = cart.getTickets().get(0);

        assertThat(cart.getTickets().size()).isEqualTo(1);
        assertThat(ticket.getId()).isEqualTo(ticketId);
        assertThat(ticket.getStatus()).isEqualTo(RESERVED);
        assertThat(ticket.getRaffle().getAvailableTickets()).isEqualTo(tickets.size() - 1);
    }

    @Test
    @Transactional
    void shouldFailReserveIfTicketsDoNotBelongToAssociationRaffle() throws Exception {
        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        List<Ticket> tickets = raffle.getTickets();

        AuthResponse authResponse = registerOtherUser();
        String otherToken = authResponse.accessToken();
        Long associationId = authResponse.association().id();
        Long cartId = createCart(getCreateCartURL(associationId), otherToken);

        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(List.of(tickets.get(0).getId()))
                .build();

        performReserveRequest(request, getReserveURL(associationId, cartId), otherToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Some tickets do not belong to an association raffle"));
    }

    @Test
    @Transactional
    void shouldFailReserveIfUserDoesNotBelongToAssociation() throws Exception {
        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        Long cartId = createCart(getCreateCartURL(associationId), accessToken);
        List<Ticket> tickets = raffle.getTickets();

        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(List.of(tickets.get(0).getId()))
                .build();

        String otherToken = registerOtherUser().accessToken();

        performReserveRequest(request, getReserveURL(associationId, cartId), otherToken)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not a member of this association"));
    }

    @Test
    @Transactional
    void shouldFailToReduceIfNotEnoughTicketsAvailable() throws Exception {
        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        raffle.setAvailableTickets(0L);
        raffle = rafflesRepository.save(raffle);
        List<Ticket> tickets = raffle.getTickets();

        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(List.of(tickets.get(0).getId()))
                .build();

        Long cartId = createCart(getCreateCartURL(associationId), accessToken);

        performReserveRequest(request, getReserveURL(associationId, cartId), accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient tickets available to complete the operation"));
    }

    @Test
    void shouldFailReserveIfTicketsDoNotExist() throws Exception {
        Long cartId = createCart(getCreateCartURL(), accessToken);

        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(List.of(999L))
                .build();

        performReserveRequest(request, getReserveURL(cartId), accessToken)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No tickets were found for provided ids"));
    }

    @Test
    void shouldFailReserveIfTicketsListIsNull() throws Exception {
        Long cartId = createCart(getCreateCartURL(), accessToken);

        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(null)
                .build();

        performReserveRequest(request, getReserveURL(cartId), accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.ticketsIds").value("Must select at least one ticket"));
    }

    @Test
    void shouldFailReserveIfTicketsListIsEmpty() throws Exception {
        Long cartId = createCart(getCreateCartURL(), accessToken);

        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(Collections.emptyList())
                .build();

        performReserveRequest(request, getReserveURL(cartId), accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.ticketsIds").value("Must select at least one ticket"));
    }

    @Test
    @Transactional
    void shouldFailReserveIfTicketsAreNotAvailable() throws Exception {
        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        List<Ticket> tickets = raffle.getTickets();
        Ticket ticket = tickets.get(0);
        ticket.setStatus(SOLD);
        ticketsRepository.save(ticket);

        Long cartId = createCart(getCreateCartURL(), accessToken);

        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(List.of(ticket.getId()))
                .build();

        performReserveRequest(request, getReserveURL(cartId), accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Some tickets are not available"));
    }

    @Test
    @Transactional
    void shouldFailIfNotEnoughTicketsAvailable() throws Exception {
        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        List<Ticket> tickets = raffle.getTickets();
        raffle.setAvailableTickets(0L);
        rafflesRepository.save(raffle);

        Long cartId = createCart(getCreateCartURL(), accessToken);

        ReservationRequest request = ReservationRequest.builder()
                .ticketsIds(List.of(tickets.get(0).getId()))
                .build();

        performReserveRequest(request, getReserveURL(cartId), accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient tickets available to complete the operation"));
    }
}