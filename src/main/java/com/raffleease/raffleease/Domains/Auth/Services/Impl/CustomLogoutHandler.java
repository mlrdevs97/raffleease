package com.raffleease.raffleease.Domains.Auth.Services.Impl;

import com.raffleease.raffleease.Domains.Auth.Services.ICookiesService;
import com.raffleease.raffleease.Domains.Token.Services.ITokensManagementService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.AuthorizationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class CustomLogoutHandler implements LogoutHandler {
    private final ITokensManagementService tokensManagementService;
    private final ICookiesService cookiesService;
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String accessToken = extractTokenFromRequest(request);
        String refreshToken = cookiesService.extractCookieValue(request, "refresh_token");
        tokensManagementService.revoke(accessToken);
        tokensManagementService.revoke(refreshToken);
        cookiesService.deleteCookie(response, "refresh_token");
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (Objects.isNull(authHeader)) throw new AuthorizationException("Missing authorization header");
        if (!authHeader.startsWith("Bearer ")) throw new AuthorizationException("Invalid token format");
        return authHeader.substring(7);
    }
}
