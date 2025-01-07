package com.raffleease.raffleease.Domains.Auth.Services;

import com.raffleease.raffleease.Domains.Auth.DTOs.AuthRequest;
import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;

public interface ILoginService {
    AuthResponse authenticate(AuthRequest request);
}
