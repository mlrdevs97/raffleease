package com.raffleease.raffleease.Domains.Auth.Services;

import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;
import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface IRegisterService {
    AuthResponse register(AssociationRegister request, HttpServletResponse response);
}
