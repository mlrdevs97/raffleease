package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Helpers.RaffleCreateBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

public class RafflesControllerUpdateStatusIT extends BaseRafflesIT {
    Long raffleId;

    @BeforeEach
    void setUp() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(2).andReturn());
        RaffleCreate createRequest = new RaffleCreateBuilder()
                .withImages(images)
                .build();
        MvcResult result = performCreateRaffleRequest(createRequest).andReturn();
        raffleId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    @Test
    void shouldUpdateStatusFromPendingToActive() {

    }

    @Test
    void shouldUpdateStatusFromPausedToActive() {

    }

    @Test
    void shouldFailUpdateStatusFromOtherStatusToActive() {

    }

    @Test
    void shouldPauseRaffleIfStatusIsActive() {

    }

    @Test
    void shouldFailPauseIfStatusIsNotActive() {

    }

    @Test
    void shouldFailIfNewStatusIsPending() {

    }

    @Test
    void shouldFailIfInvalidNewStatusIsProvided() {

    }
}
