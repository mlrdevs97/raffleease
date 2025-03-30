package com.raffleease.raffleease.Domains.Tokens.Services;

public interface TokensValidateService {
    void validateToken(String token);
    boolean isTokenValid(String token);
}
