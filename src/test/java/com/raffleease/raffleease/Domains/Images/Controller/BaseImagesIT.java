package com.raffleease.raffleease.Domains.Images.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.raffleease.raffleease.Base.BaseIT;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.DTOs.UpdateOrderRequest;
import com.raffleease.raffleease.Domains.Images.Mappers.ImagesMapper;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public abstract class BaseImagesIT extends BaseIT {
    @Autowired
    protected ImagesMapper mapper;

    protected void verifyImageOrderInRepository(List<ImageDTO> images, List<Integer> expectedOrders) {
        for (int i = 0; i < images.size(); i++) {
            Image image = imagesRepository.findById(images.get(i).id()).orElseThrow();
            assertThat(image.getImageOrder()).isEqualTo(expectedOrders.get(i));
        }
    }

    protected ResultActions sendReorderRequest(UpdateOrderRequest reorderRequest, String url, String token) throws Exception {
        return mockMvc.perform(post(url)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reorderRequest)));
    }

    protected List<Integer> extractImageOrdersFromResponse(MvcResult result) throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode imageNodes = response.path("data").path("images");
        List<Integer> orders = new ArrayList<>();
        for (JsonNode node : imageNodes) {
            orders.add(node.path("imageOrder").asInt());
        }
        return orders;
    }
}