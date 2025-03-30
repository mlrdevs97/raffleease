package com.raffleease.raffleease.Domains.Auth.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffleease.raffleease.Domains.Associations.Repository.AssociationsRepository;
import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;
import com.raffleease.raffleease.Domains.Auth.DTOs.LoginRequest;
import com.raffleease.raffleease.Domains.Users.Repository.CustomUsersRepository;
import com.raffleease.raffleease.Domains.Users.Repository.UsersRepository;
import com.raffleease.raffleease.Helpers.AssociationRegisterBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(SpringExtension.class)
public class AuthControllerLoginIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomUsersRepository customUsersRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AssociationsRepository associationsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private AssociationRegister registerRequest;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");

    @BeforeEach
    void setUp() {
        registerRequest = new AssociationRegisterBuilder().build();
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
    }

    @Test
    void shouldLoginSuccessfullyWithEmail() throws Exception {
        performSuccessfulRegister(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(registerRequest.email())
                .password(registerRequest.password())
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User authenticated successfully"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refresh_token"));
    }

    @Test
    void shouldLoginSuccessfullyWithUsername() throws Exception {
        performSuccessfulRegister(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(registerRequest.email())
                .password(registerRequest.password())
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User authenticated successfully"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refresh_token"));
    }

    @Test
    void shouldLoginSuccessfullyWithPhoneNumber() throws Exception {
        performSuccessfulRegister(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(registerRequest.phoneNumber())
                .password(registerRequest.password())
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User authenticated successfully"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refresh_token"));
    }

    @Test
    void shouldFailIfIdentifierIsMissing() throws Exception {
        performSuccessfulRegister(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(null)
                .password(registerRequest.password())
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.identifier").value("Identifier is required"));
    }

    @Test
    void shouldFailIfPasswordIsMissing() throws Exception {
        performSuccessfulRegister(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(registerRequest.email())
                .password(null)
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password is required"));
    }

    @Test
    void shouldFailIfUserNotFound() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(registerRequest.email())
                .password(registerRequest.password())
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication failed for provided credentials"));
    }

    @Test
    void shouldFailIfAuthenticationFails() throws Exception {
        performSuccessfulRegister(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(registerRequest.email())
                .password("wrongPassword#123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication failed for provided credentials"));
    }

    private void performSuccessfulRegister(AssociationRegister request) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
