package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleEdit;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Repository.RafflesRepository;
import com.raffleease.raffleease.Domains.Tickets.Repository.TicketsRepository;
import com.raffleease.raffleease.Domains.Tokens.Services.TokensQueryService;
import com.raffleease.raffleease.Helpers.RaffleCreateBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RafflesControllerUpdateIT extends BaseRafflesIT {
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
    void shouldEditRaffleSuccessfully() throws Exception {
        // 1. Upload initial images and create raffle
        List<ImageDTO> originalImages = parseImagesFromResponse(uploadImages(2).andReturn());
        RaffleCreate originalRaffle = new RaffleCreateBuilder()
                .withImages(originalImages)
                .build();

        MvcResult createResult = performCreateRaffleRequest(originalRaffle)
                .andExpect(status().isCreated())
                .andReturn();

        Long raffleId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 2. Upload new images
        List<ImageDTO> newImages = parseImagesFromResponse(uploadImages(2).andReturn());
        List<ImageDTO> reordered = IntStream.range(0, newImages.size())
                .mapToObj(i -> copyWithNewOrder(newImages.get(i), i + 1))
                .toList();

        // 3. Build edit request
        RaffleEdit editRequest = RaffleEdit.builder()
                .title("Updated Title")
                .description("Updated description")
                .endDate(LocalDateTime.now().plusDays(10))
                .images(reordered)
                .ticketPrice(new BigDecimal("2.50"))
                .totalTickets(10L)
                .price(new BigDecimal("2.50"))
                .build();

        // 4. Perform edit request
        mockMvc.perform(put("/api/v1/raffles/{id}", raffleId)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"))
                .andExpect(jsonPath("$.data.description").value("Updated description"))
                .andExpect(jsonPath("$.data.images", hasSize(3)))
                .andExpect(jsonPath("$.data.totalTickets").value(10))
                .andExpect(jsonPath("$.data.ticketPrice").value("2.50"));

        // 5. Validate images updated in DB
        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        List<Image> updatedImages = imagesRepository.findAllByRaffle(raffle);
        assertThat(updatedImages.size()).isEqualTo(3);
        // assertThat(updatedImages.stream().map(Image::getImageOrder)).containsExactlyInAnyOrder(1, 2, 3);
    }

}
