package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.raffleease.raffleease.Base.BaseIT;
import com.raffleease.raffleease.Domains.Raffles.DTOs.StatusUpdate;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

public class BaseRafflesIT extends BaseIT {
    protected ResultActions performUpdateStatusRequest(Long id, RaffleStatus status) throws Exception {
        StatusUpdate request = new StatusUpdate(status);
        return mockMvc.perform(patch("/api/v1/associations/" + associationId + "/raffles/{id}/status", id)
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
