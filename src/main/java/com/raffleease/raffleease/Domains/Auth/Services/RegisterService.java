package com.raffleease.raffleease.Domains.Auth.Services;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterRequest;
import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface RegisterService {
    void register(RegisterRequest request, HttpServletResponse response);
}
