package com.raffleease.raffleease.Base;

import com.fasterxml.jackson.databind.JsonNode;
import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterRequest;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Helpers.RaffleCreateBuilder;
import com.raffleease.raffleease.Helpers.RegisterBuilder;
import com.raffleease.raffleease.Helpers.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class BaseIT extends BaseSharedIT {
    @BeforeEach
    void setUp() throws Exception {
        AuthResponse authResponse = performAuthentication(new RegisterBuilder().build());
        accessToken = authResponse.accessToken();
        associationId = authResponse.association().id();
    }

    protected AuthResponse performAuthentication(RegisterRequest registerRequest) throws Exception {
        MvcResult result = performRegisterRequest(registerRequest).andReturn();
        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        refreshToken = result.getResponse().getCookie("refresh_token").getValue();
        return objectMapper.treeToValue(jsonNode.path("data"), AuthResponse.class);
    }

    protected AuthResponse registerOtherUser() throws Exception {
        RegisterRequest registerRequest = TestUtils.getOtherUserRegisterRequest();
        return performAuthentication(registerRequest);
    }

    protected ResultActions uploadImages(int numImages) throws Exception {
        return performImageUpload(numImages, accessToken, "/api/v1/associations/" + associationId + "/images");
    }

    protected ResultActions uploadImages(int numImages, String token) throws Exception {
        return performImageUpload(numImages, token, "/api/v1/associations/" + associationId + "/images");
    }

    protected ResultActions uploadImages(int numImages, String token, long associationId) throws Exception {
        return performImageUpload(numImages, token, "/api/v1/associations/" + associationId + "/images");
    }

    protected ResultActions uploadImagesForRaffle(int numImages, long raffleId) throws Exception {
        String url = "/api/v1/associations/" + associationId + "/raffles/" + raffleId + "/images";
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

    protected Long createRaffle() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1).andReturn());
        return createRaffle(images, accessToken);
    }

    protected Long createRaffle(String token) throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1, token).andReturn());
        return createRaffle(images, token);
    }

    protected Long createRaffle(List<ImageDTO> images, String token) throws Exception {
        RaffleCreate raffleCreate = new RaffleCreateBuilder()
                .withImages(images)
                .build();

        MvcResult result = performCreateRaffleRequest(raffleCreate, token).andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    protected ResultActions performCreateRaffleRequest(RaffleCreate raffleCreate) throws Exception {
        return executeCreateRaffleRequest(raffleCreate, accessToken);
    }

    protected ResultActions performCreateRaffleRequest(RaffleCreate raffleCreate, String token) throws Exception {
        return executeCreateRaffleRequest(raffleCreate, token);
    }

    private ResultActions executeCreateRaffleRequest(RaffleCreate raffleCreate, String token) throws Exception {
        return mockMvc.perform(post("/api/v1/associations/" + associationId + "/raffles")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(raffleCreate)));
    }
}
