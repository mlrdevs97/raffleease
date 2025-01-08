package com.raffleease.raffleease.Domains.Token.Services.Impls;

import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Auth.Services.ICookiesService;
import com.raffleease.raffleease.Domains.Token.Services.*;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Services.IUsersService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.AuthorizationException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;


@RequiredArgsConstructor
@Service
public class TokensManagementServiceImpl implements ITokensManagementService {
    private final ITokensCreateService tokensCreateService;
    private final ITokensValidateService tokensValidateService;
    private final ITokensQueryService tokensQueryService;
    private final IUsersService usersService;
    private final IBlackListService blackListService;
    private final ICookiesService cookiesService;

    @Override
    public AuthResponse refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = cookiesService.extractCookieValue(request, "refresh_token");
        tokensValidateService.validateToken(refreshToken);
        String subject = tokensQueryService.getSubject(refreshToken);
        Long userId = Long.parseLong(subject);
        User user;
        try {
            user = usersService.findById(userId);
        } catch (NotFoundException ex) {
            throw new AuthorizationException("User not found for provided subject in token");
        }
        String accessToken = tokensCreateService.generateAccessToken(user.getId());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .build();
    }

    @Override
    public void revoke(String token) {
        String tokenId = tokensQueryService.getTokenId(token);
        if (blackListService.isTokenBlackListed(tokenId)) throw new AuthorizationException("Token already revoked");
        Date expiration = tokensQueryService.getExpiration(token);
        Long expirationTime = expiration.getTime() - System.currentTimeMillis();
        blackListService.addTokenToBlackList(tokenId, expirationTime);
    }
}
