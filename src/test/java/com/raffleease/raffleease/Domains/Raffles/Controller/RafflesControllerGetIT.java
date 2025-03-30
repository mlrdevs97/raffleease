package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.Repository.RafflesRepository;
import com.raffleease.raffleease.Domains.Tickets.Repository.TicketsRepository;
import com.raffleease.raffleease.Domains.Tokens.Services.TokensQueryService;
import com.raffleease.raffleease.Helpers.RaffleCreateBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RafflesControllerGetIT extends BaseRafflesIT {
    @Autowired
    private RafflesRepository rafflesRepository;

    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private TicketsRepository ticketsRepository;

    @Autowired
    private TokensQueryService tokensQueryService;

    @Autowired
    private AssociationsService associationsService;

    @Value("${spring.storage.images.base_path}")
    private String basePath;

    @Test
    void shouldRetrieveRaffleById() throws Exception {
        RaffleCreate raffleCreate = new RaffleCreateBuilder()
                .withImages(parseImagesFromResponse(uploadImages(1).andReturn()))
                .build();

        MvcResult creationResult = performCreateRaffleRequest(raffleCreate).andReturn();

        Long raffleId = objectMapper.readTree(creationResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/raffles/" + raffleId)
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Raffle retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(raffleId))
                .andExpect(jsonPath("$.data.title").value(raffleCreate.title()));
    }

    @Test
    void shouldRetrieveAllRafflesForAssociation() throws Exception {
        RaffleCreate raffle1 = new RaffleCreateBuilder()
                .withTitle("Raffle One")
                .withImages(parseImagesFromResponse(uploadImages(1).andReturn()))
                .build();

        RaffleCreate raffle2 = new RaffleCreateBuilder()
                .withTitle("Raffle Two")
                .withImages(parseImagesFromResponse(uploadImages(1).andReturn()))
                .build();

        performCreateRaffleRequest(raffle1).andExpect(status().isCreated());
        performCreateRaffleRequest(raffle2).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/raffles")
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All raffles retrieved successfully"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].title").value("Raffle One"))
                .andExpect(jsonPath("$.data[1].title").value("Raffle Two"));
    }

    @Test
    void shouldFailWhenRaffleDoesNotExist() throws Exception {
        long nonExistentId = 99999L;
        mockMvc.perform(get("/api/v1/raffles/" + nonExistentId)
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Raffle not found for id <" + nonExistentId + ">"));
    }
}