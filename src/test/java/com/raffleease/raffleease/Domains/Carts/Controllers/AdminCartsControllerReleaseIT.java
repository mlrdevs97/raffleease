package com.raffleease.raffleease.Domains.Carts.Controllers;

import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Carts.DTO.ReservationRequest;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminCartsControllerReleaseIT extends BaseAdminCartsIT {
    Long cartId;

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
        cartId = createCart(associationId, accessToken);
        performReserveRequest(buildReleaseRequest(ticketId), associationId, cartId, accessToken);
    }

    @Test
    void shouldReleaseTickets() throws Exception {
        performReleaseRequest(buildReleaseRequest(ticketId), getReleaseURL(cartId), accessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tickets released successfully"));

        Cart cart = cartsRepository.findById(cartId).orElseThrow();
        List<Ticket> cartTickets = ticketsRepository.findAllByCart(cart);
        Ticket ticket = ticketsRepository.findById(ticketId).orElseThrow();

        assertThat(cartTickets).doesNotContain(ticket);
        assertThat(ticket.getStatus()).isEqualTo(AVAILABLE);
        assertThat(ticket.getRaffle().getAvailableTickets()).isEqualTo(tickets.size());
    }

    @Test
    void shouldFailReleaseIfTicketsDoNotExist() throws Exception {
        performReleaseRequest(buildReleaseRequest(999L), getReleaseURL(cartId), accessToken)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Some tickets were not found"));
    }

    @Test
    void shouldFailReleaseIfTicketsListIsNull() throws Exception {
        ReservationRequest request = ReservationRequest.builder().ticketsIds(null).build();

        performReleaseRequest(request, getReleaseURL(cartId), accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.ticketsIds").value("REQUIRED"));
    }

    @Test
    void shouldFailReleaseIfTicketsListIsEmpty() throws Exception {
        Long cartId = createCart(associationId, accessToken);
        ReservationRequest request = ReservationRequest.builder().ticketsIds(List.of()).build();
        performReleaseRequest(request, getReleaseURL(cartId), accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.ticketsIds").value("REQUIRED"));
    }

    @Test
    void shouldFailReleaseIfTicketsDoNotBelongToAssociationRaffle() throws Exception {
        AuthResponse authResponse = registerOtherUser();
        Long associationId = authResponse.associationId();
        String otherToken = authResponse.accessToken();

        performReleaseRequest(buildReleaseRequest(ticketId), getReleaseURL(associationId, cartId), otherToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Some tickets do not belong to an association raffle"));
    }

    @Test
    void shouldFailReleaseIfTicketsDoNotBelongToCart() throws Exception {
        Long cartId = createCart(associationId, accessToken);

        performReleaseRequest(buildReleaseRequest(ticketId), getReleaseURL(cartId), accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot release a ticket that does not belong to the cart"));
    }

    private ReservationRequest buildReleaseRequest(Long... ids) {
        return ReservationRequest.builder()
                .ticketsIds(List.of(ids))
                .build();
    }

    @Test
    void shouldFailReleaseIfUserDoesNotBelongToAssociation() throws Exception {
        String otherToken = registerOtherUser().accessToken();
        performReleaseRequest(buildReleaseRequest(ticketId), getReleaseURL(cartId), otherToken)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not a member of this association"));
    }

    @Test
    @Transactional
    void shouldFailToReduceIfReleasedTicketsExceedLimit() throws Exception {
        raffle.setAvailableTickets((long) 1);
        rafflesRepository.save(raffle);

        performReleaseRequest(buildReleaseRequest(ticketId), getReleaseURL(cartId), accessToken);

    }

    private String getReleaseURL(Long cartId) {
        return getReleaseURL(associationId, cartId);
    }

    private String getReleaseURL(Long associationId, Long cartId) {
        return "/admin/api/v1/associations/" + associationId + "/carts/" + cartId + "/reservations";
    }

    private ResultActions performReleaseRequest(ReservationRequest request, String URL, String token) throws Exception {
        return mockMvc.perform(put(URL)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
