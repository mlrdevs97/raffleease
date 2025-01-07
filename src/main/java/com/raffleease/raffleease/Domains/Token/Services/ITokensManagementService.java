package com.raffleease.raffleease.Domains.Token.Services;

import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ITokensManagementService {
    void revoke(String token);
    AuthResponse refresh(HttpServletRequest request, HttpServletResponse response);
}
