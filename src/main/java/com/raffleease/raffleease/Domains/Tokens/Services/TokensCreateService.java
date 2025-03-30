package com.raffleease.raffleease.Domains.Tokens.Services;

public interface TokensCreateService {
    String generateAccessToken(Long userId);
    String generateRefreshToken(Long userId);
}
