package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.raffleease.raffleease.Base.BaseIT;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.StatusUpdate;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;
import com.raffleease.raffleease.Domains.Tickets.Repository.TicketsRepository;
import com.raffleease.raffleease.Helpers.RaffleCreateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class BaseRafflesIT extends BaseIT {
    @Autowired
    protected TicketsRepository ticketsRepository;

    protected Long raffleId;
    protected List<ImageDTO> originalImages;

    protected void createRaffle() throws Exception {
        originalImages = parseImagesFromResponse(uploadImages(2).andReturn());

        MvcResult result = performCreateRaffleRequest(new RaffleCreateBuilder()
                .withImages(originalImages)
                .build()).andReturn();

        raffleId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    protected ResultActions performCreateRaffleRequest(RaffleCreate raffleCreate) throws Exception {
        return mockMvc.perform(post("/api/v1/raffles")
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(raffleCreate)));
    }

    protected ResultActions performUpdateStatusRequest(Long id, RaffleStatus status) throws Exception {
        StatusUpdate request = new StatusUpdate(status);
        return mockMvc.perform(patch("/api/v1/raffles/{id}/status", id)
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
