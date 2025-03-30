package com.raffleease.raffleease.Base;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffleease.raffleease.Domains.Associations.Repository.AssociationsRepository;
import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;
import com.raffleease.raffleease.Domains.Auth.DTOs.LoginRequest;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Raffles.Repository.RafflesRepository;
import com.raffleease.raffleease.Domains.Users.Repository.UsersRepository;
import com.raffleease.raffleease.Helpers.AssociationRegisterBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class BaseIT {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String accessToken;

    protected String refreshToken;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AssociationsRepository associationsRepository;

    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private RafflesRepository rafflesRepository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");

    @Container
    @ServiceConnection
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2")).withExposedPorts(6379);

    @DynamicPropertySource
    static void configureRedis(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @BeforeEach
    void setUp() throws Exception {
        accessToken = performAuthentication(new AssociationRegisterBuilder().build());
    }

    @AfterEach
    void cleanDatabase() {
        imagesRepository.deleteAll();
        rafflesRepository.deleteAll();
        associationsRepository.deleteAll();
        usersRepository.deleteAll();
    }

    @Test
    void ConnectionEstablished() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
        assertThat(redisContainer.isCreated()).isTrue();
        assertThat(redisContainer.isRunning()).isTrue();
    }

    protected String performAuthentication(AssociationRegister registerRequest) throws Exception {
        performRegister(registerRequest);

        LoginRequest loginRequest = new LoginRequest(registerRequest.email(), registerRequest.password());
        MvcResult result = performLogin(loginRequest);

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return jsonNode.path("data").path("accessToken").asText();
    }

    protected ResultActions uploadImages(int numImages) throws Exception {
        return performImageUpload(numImages, accessToken);
    }

    protected ResultActions uploadImages(int numImages, String token) throws Exception {
        return performImageUpload(numImages, token);
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

    private void performRegister(AssociationRegister request) throws Exception  {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private MvcResult performLogin(LoginRequest request) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();
    }

    private ResultActions performImageUpload(int numImages, String token) throws Exception {
        MockMultipartHttpServletRequestBuilder requestBuilder = multipart("/api/v1/images");
        requestBuilder.header(AUTHORIZATION, "Bearer " + token);
        for (int i = 0; i < numImages; i++) {
            requestBuilder.file(new MockMultipartFile("files", "image" + i + ".jpg", "image/jpeg", "testdata".getBytes()));
        }
        return mockMvc.perform(requestBuilder);
    }
}
