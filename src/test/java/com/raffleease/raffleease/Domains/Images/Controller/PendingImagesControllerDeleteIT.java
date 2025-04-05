package com.raffleease.raffleease.Domains.Images.Controller;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterRequest;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Helpers.RaffleBuilder;
import com.raffleease.raffleease.Helpers.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class PendingImagesControllerDeleteIT extends BaseImagesIT {
    private final String DELETE_IMAGE_RUL = "/api/v1/images/{id}";

    @Test
    void shouldDeleteImageSuccessfully() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(3).andReturn());

        performImageDelete(images.get(1).id())
                .andExpect(status().isNoContent());

        Association association = associationsRepository.findById(Long.parseLong(tokensQueryService.getSubject(accessToken))).orElseThrow();
        assertThat(imagesRepository.findById(images.get(1).id())).isEmpty();
        assertThat(imagesRepository.findAllByRaffleIsNullAndAssociation(association).size()).isEqualTo(2);

        List<Image> remaining = imagesRepository.findAll();
        List<Integer> orders = remaining.stream().map(Image::getImageOrder).toList();

        assertThat(orders.get(0)).isEqualTo(1);
        assertThat(orders.get(1)).isEqualTo(2);
    }

    @Test
    void shouldFailWhenImageDoesNotExist() throws Exception {
        long nonExistingId = 999L;

        performImageDelete(nonExistingId)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Image not found for id <" + nonExistingId + ">"));
    }

    @Test
    void shouldFailWhenImageDoesNotBelongToAssociation() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1).andReturn());

        RegisterRequest registerRequest = TestUtils.getOtherUserRegisterRequest();
        String otherToken = performAuthentication(registerRequest);

        performImageDelete(images.get(0).id(), otherToken)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized to delete this image"));
    }

    @Test
    void shouldReorderRemainingImagesAfterDeletion() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(4).andReturn());

        performImageDelete(images.get(1).id())
                .andExpect(status().isNoContent());

        List<Image> remaining = imagesRepository.findAll();
        List<Integer> orders = remaining.stream()
                .sorted(Comparator.comparing(Image::getImageOrder))
                .map(Image::getImageOrder)
                .toList();

        assertThat(orders.get(0)).isEqualTo(1);
        assertThat(orders.get(1)).isEqualTo(2);
        assertThat(orders.get(2)).isEqualTo(3);
    }

    private ResultActions performImageDelete(Long imageId) throws Exception {
        return sendImageDeleteRequest(imageId, accessToken);
    }

    private ResultActions performImageDelete(Long imageId, String token) throws Exception {
        return sendImageDeleteRequest(imageId, token);
    }

    private ResultActions sendImageDeleteRequest(Long imageId, String token) throws Exception {
        return mockMvc.perform(delete(DELETE_IMAGE_RUL, imageId)
                .header(AUTHORIZATION, "Bearer " + token));
    }
}