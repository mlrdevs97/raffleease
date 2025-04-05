package com.raffleease.raffleease.Domains.Tokens.Controller;

import com.raffleease.raffleease.Base.BaseIT;
import com.raffleease.raffleease.Helpers.TestUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TokensControllerIT extends BaseIT {
    private final String refreshURL = "/api/v1/tokens/refresh";

    @Test
    void shouldRefreshTokensSuccessfully() throws Exception {
        MvcResult result = performRefresh(createCookie(refreshToken))
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
        performRefresh(createCookie("malformed.token.string"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Invalid or malformed JWT")));
    }

    @Test
    void shouldFailRefreshWithoutRefreshTokenCookie() throws Exception {
        mockMvc.perform(post(refreshURL)
                .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("No cookies found")));
    }

    @Test
    void shouldFailRefreshIfRefreshTokenBlacklisted() throws Exception {
        TestUtils.blackListToken(tokensQueryService, blackListService, refreshToken);

        performRefresh(createCookie(refreshToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Token is black listed")));
    }

    @Test
    void shouldFailIfRefreshTokenIsExpired() throws Exception {
        String expiredToken = TestUtils.createExpiredToken(tokensQueryService);

        performRefresh(createCookie(expiredToken))
                .andExpect(status().isForbidden());
    }

    private ResultActions performRefresh(Cookie cookie) throws Exception {
        return mockMvc.perform(post(refreshURL)
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .cookie(cookie));
    }

    private Cookie createCookie(String token) {
        return new Cookie("refresh_token", token);
    }
}