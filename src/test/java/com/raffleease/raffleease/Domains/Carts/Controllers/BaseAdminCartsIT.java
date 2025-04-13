package com.raffleease.raffleease.Domains.Carts.Controllers;

import com.raffleease.raffleease.Base.BaseIT;
import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Carts.DTO.ReservationRequest;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BaseAdminCartsIT extends BaseIT {
    @BeforeEach
    void setUp() throws Exception {
        raffleId = createRaffle();
    }

    protected Long raffleId;

    protected String getCreateCartURL() {
        return getCreateCartURL(associationId);
    }

    protected String getCreateCartURL(Long associationId) {
        return "/admin/api/v1/associations/" + associationId + "/carts";
    }

    protected String getReserveURL(Long cartId) {
        return getReserveURL(associationId, cartId);
    }

    protected String getReserveURL(Long associationId, Long cartId) {
        return getCreateCartURL(associationId) + "/" + cartId + "/reservations";
    }

    protected Long createCart(String URL, String token) throws Exception {
        MvcResult result = performCreateCartRequest(URL, token).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    protected ResultActions performCreateCartRequest(String URL, String token) throws Exception {
        return mockMvc.perform(post(URL)
                .header(AUTHORIZATION, "Bearer " + token));
    }

    protected ResultActions performReserveRequest(ReservationRequest request, String URL, String token) throws Exception{
        return mockMvc.perform(post(URL)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
