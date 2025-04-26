package com.raffleease.raffleease.Domains.Images.Controller;

import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.DTOs.UpdateOrderRequest;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Helpers.RaffleCreateBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class PendingImagesControllerUpdateOrderIT extends BaseImagesIT {
    @Test
    void shouldUpdateImagesOrder() throws Exception {
        MvcResult result = uploadImages(3).andExpect(status().isOk()).andReturn();
        List<ImageDTO> images = parseImagesFromResponse(result);

        UpdateOrderRequest reorderRequest = new UpdateOrderRequest(List.of(
                copyWithNewOrder(images.get(2), 1),
                copyWithNewOrder(images.get(1), 2),
                copyWithNewOrder(images.get(0), 3)
        ));

        sendReorderRequest(reorderRequest, getReorderURL(), accessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image order updated successfully"))
                .andExpect(jsonPath("$.data.images", hasSize(3)));

        verifyImageOrderInRepository(reorderRequest.images(), List.of(1, 2, 3));

        List<Integer> updatedOrders = extractImageOrdersFromResponse(result);
        assertThat(updatedOrders.get(0)).isEqualTo(1);
        assertThat(updatedOrders.get(1)).isEqualTo(2);
        assertThat(updatedOrders.get(2)).isEqualTo(3);

        verifyImageOrderInRepository(reorderRequest.images(), List.of(1, 2, 3));
    }

    @Test
    void shouldFailWithDuplicateImageIds() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(2).andReturn());
        ImageDTO image = images.get(0);

        UpdateOrderRequest reorderRequest = new UpdateOrderRequest(List.of(
                copyWithNewOrder(image, 1),
                copyWithNewOrder(image, 2)
        ));

        sendReorderRequest(reorderRequest, getReorderURL(), accessToken)
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

        sendReorderRequest(reorderRequest, getReorderURL(), accessToken)
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

        sendReorderRequest(reorderRequest, getReorderURL(), accessToken)
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

        sendReorderRequest(reorderRequest, getReorderURL(), accessToken)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("One or more images were not found"));
    }

    @Test
    void shouldFailIfImageDoesNotBelongToAssociation() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1).andReturn());

        AuthResponse authResponse = registerOtherUser();
        String otherToken = authResponse.accessToken();
        Long associationId = authResponse.associationId();

        UpdateOrderRequest reorderRequest = new UpdateOrderRequest(List.of(copyWithNewOrder(images.get(0), 1)));
        sendReorderRequest(reorderRequest, getReorderURL(associationId), otherToken)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized to use the specified image(s)"));
    }

    @Test
    void shouldFailWhenAnyImageIsAlreadyLinkedToRaffle() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1).andReturn());
        RaffleCreate raffleCreate = new RaffleCreateBuilder().withImages(images).build();
        performCreateRaffleRequest(raffleCreate, associationId, accessToken);

        UpdateOrderRequest reorderRequest = new UpdateOrderRequest(List.of(copyWithNewOrder(images.get(0), 1)));
        sendReorderRequest(reorderRequest, getReorderURL(), accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("One or more images are already linked to a raffle."));
    }

    @Test
    void shouldFailWhenImageListIsEmpty() throws Exception {
        sendReorderRequest(new UpdateOrderRequest(List.of()), getReorderURL(), accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.images").value("A minimum of 1 and a maximum of 10 images are allowed"));
    }

    private String getReorderURL() {
        return "/api/v1/associations/" + associationId + "/images";
    }

    private String getReorderURL(Long associationId) {
        return "/api/v1/associations/" + associationId + "/images";
    }
}