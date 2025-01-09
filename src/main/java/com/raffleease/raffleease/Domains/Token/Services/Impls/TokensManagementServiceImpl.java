package com.raffleease.raffleease.Domains.Token.Services.Impls;

import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Auth.Services.ICookiesService;
import com.raffleease.raffleease.Domains.Token.Services.*;
import com.raffleease.raffleease.Domains.Users.Services.IUsersService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.AuthorizationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class TokensManagementServiceImpl implements ITokensManagementService {
    private final ITokensCreateService tokensCreateService;
    private final ITokensValidateService tokensValidateService;
    private final ITokensQueryService tokensQueryService;
    private final IUsersService usersService;
    private final IBlackListService blackListService;
    private final ICookiesService cookiesService;

    @Value("${spring.application.security.jwt.refresh_token_expiration}")
    private Long refreshTokenExpiration;

    @Override
    public AuthResponse refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String accessToken = extractTokenFromRequest(request);
        String refreshToken = cookiesService.getCookieValue(request, "refresh_token");
        revoke(refreshToken);
        revoke(accessToken);
        String subject = tokensQueryService.getSubject(refreshToken);
        Long userId = Long.parseLong(subject);
        if (!usersService.existsById(userId)) throw new AuthorizationException("User not found for provided subject in token");
        String newAccessToken = tokensCreateService.generateAccessToken(userId);
        String newRefreshToken = tokensCreateService.generateRefreshToken(userId);
        cookiesService.addCookie(response, "refresh_token", newRefreshToken, refreshTokenExpiration);
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    @Override
    public String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (Objects.isNull(authHeader)) throw new AuthorizationException("Missing authorization header");
        if (!authHeader.startsWith("Bearer ")) throw new AuthorizationException("Invalid token format");
        return authHeader.substring(7);
    }

    @Override
    public void revoke(String token) {
        tokensValidateService.validateToken(token);
        String tokenId = tokensQueryService.getTokenId(token);
        Date expiration = tokensQueryService.getExpiration(token);
        Long expirationTime = expiration.getTime() - System.currentTimeMillis();
        blackListService.addTokenToBlackList(tokenId, expirationTime);
    }
}
