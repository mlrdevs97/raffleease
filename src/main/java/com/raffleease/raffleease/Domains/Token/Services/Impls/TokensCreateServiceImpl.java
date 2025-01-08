package com.raffleease.raffleease.Domains.Token.Services.Impls;

import com.raffleease.raffleease.Domains.Token.Model.TokenType;
import com.raffleease.raffleease.Domains.Token.Services.ITokensCreateService;
import com.raffleease.raffleease.Domains.Token.Services.ITokensQueryService;
import com.raffleease.raffleease.Domains.Users.Model.User;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TokensCreateServiceImpl implements ITokensCreateService {
    private final ITokensQueryService tokenQueryService;

    @Override
    public String generateAccessToken(Long userId) {
        return buildToken(
                userId,
                TokenType.ACCESS,
                tokenQueryService.getAccessTokenExpirationValue()
        );
    }

    @Override
    public String generateRefreshToken(Long userId) {
        return buildToken(
                userId,
                TokenType.REFRESH,
                tokenQueryService.getRefreshTokenExpirationValue()
        );
    }

    private String buildToken(
            Long subject,
            TokenType tokenType,
            Long jwtExpiration
    ) {
        return Jwts
                .builder()
                .claim("type", tokenType.toString())
                .setId(UUID.randomUUID().toString())
                .setSubject(subject.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(tokenQueryService.getSignInKey())
                .compact();
    }
}
