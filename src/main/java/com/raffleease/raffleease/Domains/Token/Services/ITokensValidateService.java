package com.raffleease.raffleease.Domains.Token.Services;

import org.springframework.security.core.userdetails.UserDetails;

public interface ITokensValidateService {
    void validateToken(String token, UserDetails userDetails);
    boolean isTokenValid(String token, UserDetails userDetails);
}
