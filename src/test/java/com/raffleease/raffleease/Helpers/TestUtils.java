package com.raffleease.raffleease.Helpers;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterRequest;
import com.raffleease.raffleease.Domains.Tokens.Model.TokenType;
import com.raffleease.raffleease.Domains.Tokens.Services.BlackListService;
import com.raffleease.raffleease.Domains.Tokens.Services.TokensQueryService;
import io.jsonwebtoken.Jwts;

import java.util.Date;
import java.util.UUID;

public class TestUtils {
    public static String createExpiredToken(TokensQueryService tokensQueryService) {
        return Jwts.builder()
                .claim("type", TokenType.REFRESH)
                .setId(UUID.randomUUID().toString())
                .setSubject("1")
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(tokensQueryService.getSignInKey())
                .compact();
    }

    public static void blackListToken(TokensQueryService tokensQueryService, BlackListService blackListService, String token) {
        Date expiration = tokensQueryService.getExpiration(token);
        Long expirationTime = expiration.getTime() - System.currentTimeMillis();
        blackListService.addTokenToBlackList(tokensQueryService.getTokenId(token), expirationTime);
    }

    public static RegisterRequest getOtherUserRegisterRequest() {
        return new RegisterBuilder()
                .withUserName("Random Name")
                .withUserEmail("random@mail.com")
                .withUserPhone("+34", "987987654")
                .withAssociationName("Unexisting Association")
                .withAssociationPhone("+34", "987876554")
                .withAssociationEmail("random@association.com")
                .build();
    }
}
