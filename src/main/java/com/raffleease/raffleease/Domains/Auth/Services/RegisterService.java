package com.raffleease.raffleease.Domains.Auth.Services;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterRequest;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface RegisterService {
    RegisterResponse register(RegisterRequest request, HttpServletResponse response);
}
