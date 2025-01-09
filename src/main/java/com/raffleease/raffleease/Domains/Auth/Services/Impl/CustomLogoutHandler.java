package com.raffleease.raffleease.Domains.Auth.Services.Impl;

import com.raffleease.raffleease.Domains.Auth.Services.ICookiesService;
import com.raffleease.raffleease.Domains.Token.Services.ITokensManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomLogoutHandler implements LogoutHandler {
    private final ITokensManagementService tokensManagementService;
    private final ICookiesService cookiesService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String accessToken = tokensManagementService.extractTokenFromRequest(request);
        String refreshToken = cookiesService.getCookieValue(request, "refresh_token");
        tokensManagementService.revoke(accessToken);
        tokensManagementService.revoke(refreshToken);
        cookiesService.deleteCookie(response, "refresh_token");
    }
}
