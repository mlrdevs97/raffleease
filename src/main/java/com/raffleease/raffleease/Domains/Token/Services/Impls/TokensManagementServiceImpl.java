package com.raffleease.raffleease.Domains.Token.Services.Impls;

import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Auth.Services.ICookiesService;
import com.raffleease.raffleease.Domains.Token.Services.*;
import com.raffleease.raffleease.Domains.Users.Services.IUsersService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.AuthorizationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Slf4j
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

    @Value("${spring.application.config.is_test}")
    private boolean isTest;

    @Override
    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        if (isTest) log.info("Starting token refresh process.");

        String accessToken = extractTokenFromRequest(request);
        if (isTest) log.debug("Access token extracted: {}", accessToken);

        String refreshToken = cookiesService.getCookieValue(request, "refresh_token");
        if (isTest) log.debug("Refresh token extracted: {}", refreshToken);

        revoke(refreshToken);
        if (isTest) log.info("Refresh token revoked: {}", refreshToken);

        revoke(accessToken);
        if (isTest) log.info("Access token revoked: {}", accessToken);

        String subject = tokensQueryService.getSubject(refreshToken);
        Long userId = Long.parseLong(subject);
        if (isTest) log.debug("User ID extracted from refresh token: {}", userId);
        if (!usersService.existsById(userId)) {
            if (isTest) log.error("User not found for subject: {}", subject);
            throw new AuthorizationException("User not found for provided subject in token");
        }

        String newAccessToken = tokensCreateService.generateAccessToken(userId);
        if (isTest) log.info("New access token generated for user ID < {} >: {}", userId, newAccessToken);

        String newRefreshToken = tokensCreateService.generateRefreshToken(userId);
        if (isTest) log.info("New refresh token generated for user ID < {} >: {}", userId, newRefreshToken);

        cookiesService.addCookie(response, "refresh_token", newRefreshToken, refreshTokenExpiration);
        if (isTest) log.info("New refresh token added to cookies.");

        if (isTest) log.info("Token refresh process completed successfully.");
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
        if (isTest) log.info("Starting token revocation process for token: {}", token);

        tokensValidateService.validateToken(token);
        if (isTest) log.debug("Token validated: {}", token);

        String tokenId = tokensQueryService.getTokenId(token);
        Date expiration = tokensQueryService.getExpiration(token);
        Long expirationTime = expiration.getTime() - System.currentTimeMillis();

        blackListService.addTokenToBlackList(tokenId, expirationTime);
        if (isTest) log.info("Token added to blacklist with ID: {}, expiration time: {}ms", tokenId, expirationTime);
    }
}
