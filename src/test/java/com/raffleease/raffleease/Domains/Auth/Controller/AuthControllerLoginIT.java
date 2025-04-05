package com.raffleease.raffleease.Domains.Auth.Controller;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterRequest;
import com.raffleease.raffleease.Domains.Auth.DTOs.LoginRequest;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterUserData;
import com.raffleease.raffleease.Helpers.RegisterBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(SpringExtension.class)
public class AuthControllerLoginIT extends BaseAuthIT {
    private RegisterUserData userData;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest registerRequest = new RegisterBuilder().build();
        userData = registerRequest.userData();
        performRegisterRequest(registerRequest);
    }

    @Test
    void shouldLoginWithEmail() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(userData.email())
                .password(userData.password())
                .build();

        performLoginRequest(loginRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User authenticated successfully"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refresh_token"));
    }

    @Test
    void shouldLoginWithUsername() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(userData.userName())
                .password(userData.password())
                .build();

        performLoginRequest(loginRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User authenticated successfully"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refresh_token"));
    }

    @Test
    void shouldLoginWithPhoneNumber() throws Exception {
        String phoneNumber = userData.phoneNumber().prefix() + userData.phoneNumber().nationalNumber();
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(phoneNumber)
                .password(userData.password())
                .build();

        performLoginRequest(loginRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User authenticated successfully"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refresh_token"));
    }

    @Test
    void shouldFailIfIdentifierIsMissing() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(null)
                .password(userData.password())
                .build();

        performLoginRequest(loginRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.identifier").value("Identifier is required"));
    }

    @Test
    void shouldFailIfPasswordIsMissing() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(userData.email())
                .password(null)
                .build();

        performLoginRequest(loginRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password is required"));
    }

    @Test
    void shouldFailIfUserNotFound() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier("unexisting@mail.com")
                .password("MySecurePassword#123")
                .build();

        performLoginRequest(loginRequest)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Authentication failed for provided credentials")));
    }

    @Test
    void shouldFailIfAuthenticationFails() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(userData.email())
                .password("wrongPassword#123")
                .build();

        performLoginRequest(loginRequest)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Authentication failed for provided credentials")));
    }
}
