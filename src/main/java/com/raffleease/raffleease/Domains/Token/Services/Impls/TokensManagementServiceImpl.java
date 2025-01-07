package com.raffleease.raffleease.Domains.Token.Services.Impls;

import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Token.Services.*;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Model.UserPrincipal;
import com.raffleease.raffleease.Domains.Users.Services.IUsersService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.AuthorizationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

    @Override
    public AuthResponse refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        final String refreshToken = extractTokenFromRequest(request);
        final String subject = tokensQueryService.getSubject(refreshToken);

        if (Objects.isNull(subject)) throw new AuthorizationException("Subject not found in refresh token");
        User user = usersService.findByIdentifier(subject);
        UserPrincipal principal = new UserPrincipal(user);
        tokensValidateService.validateToken(refreshToken, principal);
        String accessToken = tokensCreateService.generateAccessToken(principal);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void revoke(String token) {
        String tokenId = tokensQueryService.getTokenId(token);
        Date expiration = tokensQueryService.getExpiration(token);
        Long expirationTime = expiration.getTime() - System.currentTimeMillis();
        blackListService.addTokenToBlackList(tokenId, expirationTime);
    }


    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (Objects.isNull(authHeader)) throw new AuthorizationException("Missing refresh token");
        if (!authHeader.startsWith("Bearer ")) throw new AuthorizationException("Invalid token format");
        return authHeader.substring(7);
    }
}
