package com.raffleease.raffleease.Domains.Images.Controller;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.DTOs.UpdateOrderRequest;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Helpers.RaffleBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class ImagesControllerUpdateOrderIT extends BaseImagesIT {
    private Raffle raffle;
    private List<Image> originalImages;

    @BeforeEach
    void setUp() throws Exception {
        List<ImageDTO> imageDTOs = parseImagesFromResponse(uploadImages(2).andReturn());
        Long associationId = Long.parseLong(tokensQueryService.getSubject(accessToken));
        Association association = associationsRepository.findById(associationId).orElseThrow();
        raffle = rafflesRepository.save(new RaffleBuilder(association).build());

        originalImages = new ArrayList<>();
        for (int i = 0; i < imageDTOs.size(); i++) {
            long imageId = imageDTOs.get(i).id();

            Image image = imagesRepository.findById(imageId).orElseThrow();
            image.setRaffle(raffle);
            image.setImageOrder(i + 1);

            originalImages.add(imagesRepository.save(image));
        }
    }

    @Test
    void shouldReorderAssociatedAndPendingImages() throws Exception {
        List<ImageDTO> associated = mapper.fromImagesList(originalImages);

        List<ImageDTO> pending = parseImagesFromResponse(uploadImages(2).andReturn());
        UpdateOrderRequest reorderRequest = new UpdateOrderRequest(List.of(
                copyWithNewOrder(pending.get(1), 1),
                copyWithNewOrder(associated.get(0), 2),
                copyWithNewOrder(pending.get(0), 3),
                copyWithNewOrder(associated.get(1), 4)
        ));

        MvcResult result = sendReorderRequest(reorderRequest, reorderURL(raffle.getId()), accessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image order updated successfully"))
                .andExpect(jsonPath("$.data.images", hasSize(4)))
                .andReturn();

        List<Integer> updatedOrders = extractImageOrdersFromResponse(result);
        assertThat(updatedOrders.get(0)).isEqualTo(1);
        assertThat(updatedOrders.get(1)).isEqualTo(2);
        assertThat(updatedOrders.get(2)).isEqualTo(3);
        assertThat(updatedOrders.get(3)).isEqualTo(4);

        verifyImageOrderInRepository(reorderRequest.images(), List.of(1, 2, 3, 4));
    }

    @Test
    void shouldFailWithDuplicateImageIds() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1).andReturn());
        ImageDTO img = images.get(0);

        UpdateOrderRequest reorderRequest = new UpdateOrderRequest(List.of(
                copyWithNewOrder(img, 1),
                copyWithNewOrder(img, 2)
        ));

        sendReorderRequest(reorderRequest, reorderURL(raffle.getId()), accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duplicate image IDs found in request"));
    }

    @Test
    void shouldFailWithDuplicateImageOrders() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(2).andReturn());

        UpdateOrderRequest reorderRequest = new UpdateOrderRequest(List.of(
                copyWithNewOrder(images.get(0), 1),
                copyWithNewOrder(images.get(1), 1)
        ));

        sendReorderRequest(reorderRequest, reorderURL(raffle.getId()), accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duplicate image orders detected"));
    }

    @Test
    void shouldFailWithNonConsecutiveOrders() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(2).andReturn());

        UpdateOrderRequest reorderRequest = new UpdateOrderRequest(List.of(
                copyWithNewOrder(images.get(0), 1),
                copyWithNewOrder(images.get(1), 3)
        ));

        sendReorderRequest(reorderRequest, reorderURL(raffle.getId()), accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Image orders must be consecutive starting from 1"));
    }

    @Test
    void shouldFailIfImageDoesNotExistInDb() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(2).andReturn());
        ImageDTO uploadedImage = copyWithNewOrder(images.get(1), 1);
        ImageDTO invalid = ImageDTO.builder()
                .id(999L)
                .imageOrder(uploadedImage.imageOrder())
                .fileName(uploadedImage.fileName())
                .contentType(uploadedImage.contentType())
                .url(uploadedImage.url())
                .filePath(uploadedImage.filePath())
                .build();

        UpdateOrderRequest reorderRequest = new UpdateOrderRequest(List.of(
                copyWithNewOrder(images.get(0), 2),
                invalid
        ));

        sendReorderRequest(reorderRequest, reorderURL(raffle.getId()), accessToken)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("One or more images were not found"));
    }

    @Test
    void shouldFailIfImageIsLinkedToAnotherRaffle() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1).andReturn());

        long associationId = Long.parseLong(tokensQueryService.getSubject(accessToken));
        Association association = associationsRepository.findById(associationId).orElseThrow();

        Raffle otherRaffle = rafflesRepository.save(new RaffleBuilder(association).build());

        long imageId = images.get(0).id();
        Image image = imagesRepository.findById(imageId).orElseThrow();

        image.setRaffle(otherRaffle);
        imagesRepository.save(image);

        UpdateOrderRequest reorderRequest = new UpdateOrderRequest(List.of(
                copyWithNewOrder(images.get(0), 1)
        ));

        sendReorderRequest(reorderRequest, reorderURL(raffle.getId()), accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("already linked to another raffle")));
    }

    private String reorderURL(Long raffleId) {
        return "/api/v1/raffles/" + raffleId + "/images/order";
    }
}