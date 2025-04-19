package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Helpers.RaffleCreateBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RafflesControllerGetIT extends BaseRafflesIT {
    @Value("${spring.storage.images.base_path}")
    private String basePath;

    @Test
    void shouldRetrieveRaffleById() throws Exception {
        RaffleCreate raffleCreate = new RaffleCreateBuilder()
                .withImages(parseImagesFromResponse(uploadImages(1).andReturn()))
                .build();

        MvcResult creationResult = performCreateRaffleRequest(raffleCreate, associationId, accessToken).andReturn();

        Long raffleId = objectMapper.readTree(creationResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/associations/" + associationId + "/raffles/" + raffleId)
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
        performCreateRaffleRequest(raffle1, associationId, accessToken).andExpect(status().isCreated());

        RaffleCreate raffle2 = new RaffleCreateBuilder()
                .withTitle("Raffle Two")
                .withImages(parseImagesFromResponse(uploadImages(1).andReturn()))
                .build();
        performCreateRaffleRequest(raffle2, associationId, accessToken).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/associations/" + associationId + "/raffles")
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
        mockMvc.perform(get("/api/v1/associations/" + associationId + "/raffles/" + nonExistentId)
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Raffle not found for id <" + nonExistentId + ">"));
    }
}