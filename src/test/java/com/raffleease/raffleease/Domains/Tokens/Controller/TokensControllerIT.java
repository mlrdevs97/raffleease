package com.raffleease.raffleease.Domains.Tokens.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffleease.raffleease.Domains.Associations.Repository.AssociationsRepository;
import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;
import com.raffleease.raffleease.Domains.Auth.DTOs.LoginRequest;
import com.raffleease.raffleease.Domains.Tokens.Services.BlackListService;
import com.raffleease.raffleease.Domains.Tokens.Services.TokensQueryService;
import com.raffleease.raffleease.Domains.Users.Repository.UsersRepository;
import com.raffleease.raffleease.Helpers.AssociationRegisterBuilder;
import com.raffleease.raffleease.Helpers.TestUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(SpringExtension.class)
class TokensControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AssociationsRepository associationsRepository;

    @Autowired
    private TokensQueryService tokensQueryService;

    @Autowired
    private BlackListService blackListService;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;
    private String refreshToken;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");

    @Container
    @ServiceConnection
    public static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2")).withExposedPorts(6379);

    @DynamicPropertySource
    static void configureRedis(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @BeforeEach
    void setUp() throws Exception {
        AssociationRegister registerRequest = new AssociationRegisterBuilder().build();
        performRegister(registerRequest);

        LoginRequest loginRequest = new LoginRequest(registerRequest.email(), registerRequest.password());
        MvcResult result = performLogin(loginRequest);

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        accessToken = jsonNode.path("data").path("accessToken").asText();
        refreshToken = result.getResponse().getCookie("refresh_token").getValue();
    }

    @AfterEach
    void cleanDatabase() {
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

    @Test
    void shouldRefreshTokensSuccessfully() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/tokens/refresh")
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        String newAccessToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        assertThat(newAccessToken).isNotEqualTo(accessToken);
        blackListService.isTokenBlackListed(tokensQueryService.getTokenId(accessToken));
        blackListService.isTokenBlackListed(tokensQueryService.getTokenId(refreshToken));
    }

    @Test
    void shouldFailWithMalformedRefreshToken() throws Exception {
        mockMvc.perform(post("/api/v1/tokens/refresh")
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .cookie(new Cookie("refresh_token", "malformed.token.string")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Invalid or malformed JWT")));
    }

    @Test
    void shouldFailRefreshWithoutRefreshTokenCookie() throws Exception {
        mockMvc.perform(post("/api/v1/tokens/refresh")
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("No cookies found")));
    }

    @Test
    void shouldFailRefreshIfRefreshTokenBlacklisted() throws Exception {
        TestUtils.blackListToken(tokensQueryService, blackListService, refreshToken);

        mockMvc.perform(post("/api/v1/tokens/refresh")
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Token is black listed")));
    }

    @Test
    void shouldFailIfRefreshTokenIsExpired() throws Exception {
        String expiredToken = TestUtils.createExpiredToken(tokensQueryService);

        mockMvc.perform(post("/api/v1/tokens/refresh")
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .cookie(new Cookie("refresh_token", expiredToken)))
                .andExpect(status().isForbidden());
    }

    private void performRegister(AssociationRegister request) throws Exception {
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
}