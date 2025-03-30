package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.raffleease.raffleease.Base.BaseIT;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class BaseRafflesIT extends BaseIT {
    protected ResultActions performCreateRaffleRequest(RaffleCreate raffleCreate) throws Exception{
        MockHttpServletRequestBuilder requestBuilder = post("/api/v1/raffles")
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(raffleCreate));
        return mockMvc.perform(requestBuilder);
    }
}
