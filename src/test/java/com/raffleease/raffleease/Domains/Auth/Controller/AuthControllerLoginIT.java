package com.raffleease.raffleease.Domains.Auth.Controller;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterRequest;
import com.raffleease.raffleease.Domains.Auth.DTOs.LoginRequest;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterUserData;
import com.raffleease.raffleease.Domains.Auth.Model.VerificationToken;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Helpers.RegisterBuilder;
import jakarta.transaction.Transactional;
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
    @Transactional
    void setUp() throws Exception {
        RegisterRequest registerRequest = new RegisterBuilder().build();
        performRegisterRequest(registerRequest);
        userData = registerRequest.userData();
        User user = usersRepository.findByIdentifier(userData.email()).orElseThrow();
        VerificationToken verificationToken = verificationTokenRepository.findByUser(user).orElseThrow();
        performVerificationRequest(verificationToken.getToken());
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
                .andExpect(jsonPath("$.errors.identifier").value("REQUIRED"));
    }

    @Test
    void shouldFailIfPasswordIsMissing() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(userData.email())
                .password(null)
                .build();

        performLoginRequest(loginRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("REQUIRED"));
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
