package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.raffleease.raffleease.Base.BaseIT;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Tickets.Repository.TicketsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class BaseRafflesIT extends BaseIT {
    @Autowired
    protected TicketsRepository ticketsRepository;

    protected ResultActions performCreateRaffleRequest(RaffleCreate raffleCreate) throws Exception {
        return mockMvc.perform(post("/api/v1/raffles")
                        .header(org.springframework.http.HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(raffleCreate)));
    }
}
