package com.raffleease.raffleease.Base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffleease.raffleease.Domains.Associations.Repository.AssociationsRepository;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterRequest;
import com.raffleease.raffleease.Domains.Auth.DTOs.LoginRequest;
import com.raffleease.raffleease.Domains.Auth.DTOs.RegisterEmailVerificationRequest;
import com.raffleease.raffleease.Domains.Auth.Repository.VerificationTokenRepository;
import com.raffleease.raffleease.Domains.Carts.Repository.CartsRepository;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Orders.Repository.OrdersRepository;
import com.raffleease.raffleease.Domains.Raffles.Repository.RafflesRepository;
import com.raffleease.raffleease.Domains.Tickets.Repository.TicketsRepository;
import com.raffleease.raffleease.Domains.Tokens.Services.BlackListService;
import com.raffleease.raffleease.Domains.Tokens.Services.TokensQueryService;
import com.raffleease.raffleease.Domains.Users.Repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class BaseSharedIT {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected VerificationTokenRepository verificationTokenRepository;

    @Autowired
    protected UsersRepository usersRepository;

    @Autowired
    protected AssociationsRepository associationsRepository;

    @Autowired
    protected ImagesRepository imagesRepository;

    @Autowired
    protected RafflesRepository rafflesRepository;

    @Autowired
    protected TicketsRepository ticketsRepository;

    @Autowired
    protected TokensQueryService tokensQueryService;

    @Autowired
    protected BlackListService blackListService;

    @Autowired
    protected CartsRepository cartsRepository;

    @Autowired
    protected OrdersRepository ordersRepository;

    protected String accessToken;
    protected String refreshToken;
    protected Long associationId;

    protected final String REGISTER_URL = "/api/v1/auth/register";
    protected final String LOGIN_URL = "/api/v1/auth/login";

    private final Path TEST_FILES_DIR = Paths.get("test-files");

    @Container
    @ServiceConnection
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");

    @Container
    @ServiceConnection
    protected static GenericContainer<?> redisContainer =
            new GenericContainer<>(DockerImageName.parse("redis:7.2")).withExposedPorts(6379);

    @BeforeEach
    void setUp() throws Exception {
        cleanTestImagesDirectory();
    }

    @AfterEach
    void cleanDatabase() throws Exception {
        verificationTokenRepository.deleteAll();
        imagesRepository.deleteAll();
        ticketsRepository.deleteAll();
        cartsRepository.deleteAll();
        rafflesRepository.deleteAll();
        ordersRepository.deleteAll();
        associationsRepository.deleteAll();
        usersRepository.deleteAll();
        cleanTestImagesDirectory();
    }

    @Test
    void ConnectionEstablished() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
        assertThat(redisContainer.isCreated()).isTrue();
        assertThat(redisContainer.isRunning()).isTrue();
    }

    protected ResultActions performRegisterRequest(RegisterRequest request) throws Exception{
        return mockMvc.perform(post(REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    protected ResultActions performLoginRequest(LoginRequest loginRequest) throws Exception {
        return mockMvc.perform(post(LOGIN_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));
    }

    protected ResultActions performVerificationRequest(String token) throws Exception {
        RegisterEmailVerificationRequest request = RegisterEmailVerificationRequest.builder().verificationToken(token).build();
        return mockMvc.perform(post("/api/v1/auth/verify")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private void cleanTestImagesDirectory() throws IOException {
        if (Files.exists(TEST_FILES_DIR)) {
            Files.walk(TEST_FILES_DIR)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            System.err.println("Failed to delete " + file);
                        }
                    });
        }
    }
}
