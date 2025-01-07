package com.raffleease.raffleease.Domains.Token.Services;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import io.jsonwebtoken.Claims;

public interface ITokensQueryService {
    String getSubject(String token);
    String getTokenId(String token);
    <T> T getClaim(String token, Function<Claims, T> claimsResolver);
    Key getSignInKey();
    Long getAccessTokenExpirationValue();
    Long getRefreshTokenExpirationValue();
    Date getExpiration(String token);

}
