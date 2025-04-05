package com.raffleease.raffleease.Domains.Auth.Controller;

import com.raffleease.raffleease.Base.BaseIT;
import com.raffleease.raffleease.Domains.Tokens.Services.BlackListService;
import com.raffleease.raffleease.Helpers.TestUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerValidateIT extends BaseIT {
    @Autowired
    private BlackListService blackListService;

    private String validateURL = "/api/v1/auth/validate";

    @Test
    void shouldAccessSecuredEndpointWithValidToken() throws Exception {
        mockMvc.perform(get(validateURL)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                ).andExpect(status().isOk());
    }

    @Test
    void shouldFailAccessWithoutToken() throws Exception {
        mockMvc.perform(get(validateURL))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailAccessWithInvalidToken() throws Exception {
        mockMvc.perform(get(validateURL)
                        .header(AUTHORIZATION, "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailIfAccessTokenIsExpired() throws Exception {
        String expiredToken = TestUtils.createExpiredToken(tokensQueryService);

        mockMvc.perform(get(validateURL)
                        .header(AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailIfAccessTokenBlacklisted() throws Exception {
        TestUtils.blackListToken(tokensQueryService, blackListService, accessToken);

        mockMvc.perform(post(validateURL)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isForbidden());
    }
}