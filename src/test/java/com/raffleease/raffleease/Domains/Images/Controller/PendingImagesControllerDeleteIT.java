package com.raffleease.raffleease.Domains.Images.Controller;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Helpers.AssociationBuilder;
import com.raffleease.raffleease.Helpers.AssociationRegisterBuilder;
import com.raffleease.raffleease.Helpers.RaffleBuilder;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class PendingImagesControllerDeleteIT extends BaseImagesIT {
    private final String deleteUrlTemplate = "/api/v1/images/{id}";

    @Test
    void shouldDeleteImageSuccessfully() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(3).andReturn());

        mockMvc.perform(delete(deleteUrlTemplate, images.get(1).id())
                        .header(AUTHORIZATION, "Bearer " + accessToken))
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
    void shouldFailWhenImageIsAssociatedToRaffle() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1).andReturn());
        Image image = imagesRepository.findById(images.get(0).id()).orElseThrow();
        Association association = associationsRepository.findAll().get(0);
        Raffle raffle = rafflesRepository.save(new RaffleBuilder(association).build());
        image.setRaffle(raffle);
        imagesRepository.save(image);

        mockMvc.perform(delete(deleteUrlTemplate, image.getId())
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You cannot delete an image already associated with a raffle"));
    }

    @Test
    void shouldFailWhenImageDoesNotExist() throws Exception {
        mockMvc.perform(delete(deleteUrlTemplate, 9999L)
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Image not found for id <9999>"));
    }

    @Test
    void shouldFailWhenImageDoesNotBelongToAssociation() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1).andReturn());

        Association association = new AssociationBuilder().build();
        String otherToken = performAuthentication(new AssociationRegisterBuilder()
                .withEmail(association.getEmail())
                .withPhoneNumber(association.getPhoneNumber())
                .withName(association.getAssociationName())
                .build());

        mockMvc.perform(delete(deleteUrlTemplate, images.get(0).id())
                        .header(AUTHORIZATION, "Bearer " + otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized to delete this image"));
    }

    @Test
    void shouldReorderRemainingImagesAfterDeletion() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(4).andReturn());

        mockMvc.perform(delete(deleteUrlTemplate, images.get(1).id())
                        .header(AUTHORIZATION, "Bearer " + accessToken))
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
}