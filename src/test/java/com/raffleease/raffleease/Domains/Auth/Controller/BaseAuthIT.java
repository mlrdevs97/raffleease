package com.raffleease.raffleease.Domains.Auth.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffleease.raffleease.Domains.Associations.Repository.AssociationsRepository;
import com.raffleease.raffleease.Domains.Auth.DTOs.LoginRequest;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterRequest;
import com.raffleease.raffleease.Domains.Users.Repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class BaseAuthIT {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String accessToken;

    protected String refreshToken;

    @Autowired
    protected UsersRepository usersRepository;

    @Autowired
    protected AssociationsRepository associationsRepository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");

    @Container
    @ServiceConnection
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2")).withExposedPorts(6379);

    @Test
    void ConnectionEstablished() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
        assertThat(redisContainer.isCreated()).isTrue();
        assertThat(redisContainer.isRunning()).isTrue();
    }

    protected ResultActions performRegisterRequest(RegisterRequest request) throws Exception{
        return mockMvc.perform(post("/api/v1/auth/register")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    protected ResultActions performLoginRequest(LoginRequest loginRequest) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));
    }
}
