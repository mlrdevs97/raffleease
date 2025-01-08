package com.raffleease.raffleease.Domains.Token.Services;

public interface ITokensCreateService {
    String generateAccessToken(Long userId);
    String generateRefreshToken(Long userId);
}
