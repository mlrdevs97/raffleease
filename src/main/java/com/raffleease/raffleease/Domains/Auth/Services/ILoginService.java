package com.raffleease.raffleease.Domains.Auth.Services;

import com.raffleease.raffleease.Domains.Auth.DTOs.AuthRequest;
import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface ILoginService {
    AuthResponse authenticate(AuthRequest request, HttpServletResponse response);
}
