package com.raffleease.raffleease.Domains.Orders.Controller;

import com.raffleease.raffleease.Base.BaseIT;
import com.raffleease.raffleease.Domains.Carts.DTO.ReservationRequest;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Orders.DTOs.AdminOrderCreate;
import com.raffleease.raffleease.Domains.Orders.Repository.OrdersRepository;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Helpers.AdminOrderCreateBuilder;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class BaseAminOrdersIT extends BaseIT {
    @Autowired
    protected OrdersRepository ordersRepository;
    protected Raffle raffle;
    protected Long raffleId;
    protected Long reservedTicket;
    protected Cart cart;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(2).andReturn());
        raffleId = createRaffle(images, associationId, accessToken);
        raffle = rafflesRepository.findById(raffleId).orElseThrow();
    }

    protected void createAndReserveTicketsForCart(Long associationId, String accessToken) throws Exception {
        Long cartId = createCart(associationId, accessToken);
        List<Ticket> tickets = ticketsRepository.findAllByRaffle(raffle);
        reservedTicket = tickets.get(0).getId();
        ReservationRequest request = ReservationRequest.builder().ticketsIds(List.of(reservedTicket)).build();
        performReserveRequest(request, associationId, cartId, accessToken).andReturn();
        cart = cartsRepository.findById(cartId).orElseThrow();
    }

    protected Long createOrder(Long associationId, String accessToken) throws Exception {
        AdminOrderCreate request = new AdminOrderCreateBuilder()
                .withCartId(cart.getId())
                .withTicketIds(List.of(reservedTicket))
                .build();
        return parseOrderId(performCreateOrderRequest(request, associationId, accessToken).andReturn());
    }

    protected ResultActions performCreateOrderRequest(AdminOrderCreate request, Long associationId, String token) throws Exception {
        return mockMvc.perform(post("/admin/api/v1/associations/" + associationId + "/orders")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    protected Long parseOrderId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }
}
