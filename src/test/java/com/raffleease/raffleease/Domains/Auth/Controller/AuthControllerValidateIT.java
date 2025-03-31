package com.raffleease.raffleease.Domains.Auth.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterRequest;
import com.raffleease.raffleease.Domains.Auth.DTOs.LoginRequest;
import com.raffleease.raffleease.Domains.Tokens.Services.BlackListService;
import com.raffleease.raffleease.Domains.Tokens.Services.TokensQueryService;
import com.raffleease.raffleease.Helpers.RegisterBuilder;
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
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(SpringExtension.class)
public class AuthControllerValidateIT extends BaseAuthIT {
    @Autowired
    private TokensQueryService tokensQueryService;

    @Autowired
    private BlackListService blackListService;

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
        MvcResult result = performAuthentication(new RegisterBuilder().build());

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        accessToken = jsonNode.path("data").path("accessToken").asText();
        refreshToken = result.getResponse().getCookie("refresh_token").getValue();
    }

    @Test
    void shouldAccessSecuredEndpointWithValidToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate")
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                ).andExpect(status().isOk());
    }

    @Test
    void shouldFailAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailAccessWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate")
                        .header(AUTHORIZATION, "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailIfAccessTokenIsExpired() throws Exception {
        String expiredToken = TestUtils.createExpiredToken(tokensQueryService);

        mockMvc.perform(get("/api/v1/auth/validate")
                        .header(AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailRefreshIfAccessTokenBlacklisted() throws Exception {
        TestUtils.blackListToken(tokensQueryService, blackListService, accessToken);

        mockMvc.perform(post("/api/v1/auth/validate")
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isForbidden());
    }

    private MvcResult performAuthentication(RegisterRequest registerRequest) throws Exception {
        performRegisterRequest(registerRequest);
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(registerRequest.userData().email())
                .password(registerRequest.userData().password())
                .build();
        return performLoginRequest(loginRequest).andReturn();
    }
}