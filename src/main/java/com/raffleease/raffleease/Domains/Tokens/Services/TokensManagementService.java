package com.raffleease.raffleease.Domains.Tokens.Services;

import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface TokensManagementService {
    void revoke(String token);

    AuthResponse refresh(HttpServletRequest request, HttpServletResponse response);

    String extractTokenFromRequest(HttpServletRequest request);
}
