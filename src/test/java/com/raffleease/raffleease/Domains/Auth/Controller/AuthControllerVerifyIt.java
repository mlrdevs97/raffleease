package com.raffleease.raffleease.Domains.Auth.Controller;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterRequest;
import com.raffleease.raffleease.Domains.Auth.Model.VerificationToken;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Helpers.RegisterBuilder;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerVerifyIT extends BaseAuthIT {
    @Test
    @Transactional
    void shouldVerifyEmailWithValidToken() throws Exception {
        RegisterRequest registerRequest = new RegisterBuilder().build();
        performRegisterRequest(registerRequest);
        User user = usersRepository.findByIdentifier(registerRequest.userData().email()).orElseThrow();
        VerificationToken verificationToken = verificationTokenRepository.findByUser(user).orElseThrow();

        performVerificationRequest(verificationToken.getToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account verified successfully"));

        User verified = usersRepository.findById(user.getId()).orElseThrow();
        assertThat(verified.isEnabled()).isTrue();
        assertThat(verificationTokenRepository.findByToken(verificationToken.getToken())).isEmpty();
    }

    @Test
    @Transactional
    void shouldFailVerificationWhenTokenNotFound() throws Exception {
        performVerificationRequest("invalid-token")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Verification token not found"));
    }

    @Test
    @Transactional
    void shouldFailVerificationWhenTokenIsExpired() throws Exception {
        RegisterRequest registerRequest = new RegisterBuilder().build();
        performRegisterRequest(registerRequest);
        User user = usersRepository.findByIdentifier(registerRequest.userData().email()).orElseThrow();
        VerificationToken verificationToken = verificationTokenRepository.findByUser(user).orElseThrow();
        verificationToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        verificationTokenRepository.save(verificationToken);

        performVerificationRequest(verificationToken.getToken())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Verification token is expired"));

        assertThat(verificationTokenRepository.findByToken(verificationToken.getToken())).isPresent();
        assertThat(usersRepository.findById(user.getId()).orElseThrow().isEnabled()).isFalse();
    }

    @Test
    @Transactional
    void shouldHandleAlreadyEnabledUserGracefully() throws Exception {
        RegisterRequest registerRequest = new RegisterBuilder().build();
        performRegisterRequest(registerRequest);
        User user = usersRepository.findByIdentifier(registerRequest.userData().email()).orElseThrow();
        VerificationToken verificationToken = verificationTokenRepository.findByUser(user).orElseThrow();

        performVerificationRequest(verificationToken.getToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account verified successfully"));

        assertThat(verificationTokenRepository.findByToken(verificationToken.getToken())).isEmpty();
    }
}