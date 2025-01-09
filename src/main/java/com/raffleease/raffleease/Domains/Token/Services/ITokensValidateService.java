package com.raffleease.raffleease.Domains.Token.Services;

public interface ITokensValidateService {
    void validateToken(String token);
    boolean isTokenValid(String token);
}
