package com.raffleease.raffleease.Domains.Token.Services;

import org.springframework.security.core.userdetails.UserDetails;

public interface ITokensValidateService {
    void validateToken(String token);
    boolean isTokenValid(String token);
}
