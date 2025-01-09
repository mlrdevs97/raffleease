package com.raffleease.raffleease.Domains.Token.Services.Impls;

import com.raffleease.raffleease.Domains.Token.Services.ITokensQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@RequiredArgsConstructor
@Service
public class TokensQueryServiceImpl implements ITokensQueryService {
    @Value("${spring.application.security.jwt.secret_key}")
    private String secretKey;

    @Value("${spring.application.security.jwt.access_token_expiration}")
    private Long accessTokenExpiration;

    @Value("${spring.application.security.jwt.refresh_token_expiration}")
    private Long refreshTokenExpiration;

    @Override
    public String getSubject(String token) {
        return getClaim(token, Claims::getSubject);
    }

    @Override
    public String getTokenId(String token) { return getClaim(token, Claims::getId); }

    @Override
    public <T> T getClaim(String token, Function<Claims,T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public Long getAccessTokenExpirationValue() {
        return accessTokenExpiration;
    }

    @Override
    public Long getRefreshTokenExpirationValue() {
        return refreshTokenExpiration;
    }

    @Override
    public Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
