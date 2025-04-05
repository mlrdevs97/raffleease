package com.raffleease.raffleease.Base;

import com.fasterxml.jackson.databind.JsonNode;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterRequest;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Helpers.RegisterBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

public class BaseIT extends BaseSharedIT {
    @BeforeEach
    void setUp() throws Exception {
        accessToken = performAuthentication(new RegisterBuilder().build());
    }

    protected String performAuthentication(RegisterRequest registerRequest) throws Exception {
        MvcResult result = performRegisterRequest(registerRequest).andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        refreshToken = result.getResponse().getCookie("refresh_token").getValue();
        return jsonNode.path("data").path("accessToken").asText();
    }

    protected ResultActions uploadImages(int numImages) throws Exception {
        return performImageUpload(numImages, accessToken, "/api/v1/images");
    }

    protected ResultActions uploadImages(int numImages, String token) throws Exception {
        return performImageUpload(numImages, token, "/api/v1/images");
    }

    protected ResultActions uploadImagesForRaffle(int numImages, long raffleId) throws Exception {
        String url = "/api/v1/raffles/" + raffleId + "/images";
        return performImageUpload(numImages, accessToken, url);
    }

    protected List<ImageDTO> parseImagesFromResponse(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode imagesNode = json.path("data").path("images");
        List<ImageDTO> imageList = new ArrayList<>();
        for (JsonNode node : imagesNode) {
            imageList.add(ImageDTO.builder()
                    .id(node.path("id").asLong())
                    .fileName(node.path("fileName").asText())
                    .filePath(node.path("filePath").asText())
                    .contentType(node.path("contentType").asText())
                    .url(node.path("url").asText())
                    .imageOrder(node.path("imageOrder").asInt())
                    .build());
        }
        return imageList;
    }

    protected ImageDTO copyWithNewOrder(ImageDTO image, Integer order) {
        return ImageDTO.builder()
                .id(image.id())
                .fileName(image.fileName())
                .filePath(image.filePath())
                .contentType(image.contentType())
                .url(image.url())
                .imageOrder(order)
                .build();
    }

    private ResultActions performImageUpload(int numImages, String token, String url) throws Exception {
        MockMultipartHttpServletRequestBuilder requestBuilder = multipart(url);
        requestBuilder.header(AUTHORIZATION, "Bearer " + token);
        for (int i = 0; i < numImages; i++) {
            requestBuilder.file(new MockMultipartFile("files", "image" + i + ".jpg", "image/jpeg", "testdata".getBytes()));
        }
        return mockMvc.perform(requestBuilder);
    }

    protected ResultActions performImageDelete(String url) throws Exception {
        return mockMvc.perform(delete(url)
                .header(AUTHORIZATION, "Bearer " + accessToken));
    }
}
