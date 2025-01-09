package com.raffleease.raffleease.Domains.Auth.Services.Impl;

import com.raffleease.raffleease.Domains.Auth.DTOs.LoginRequest;
import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Auth.Services.ICookiesService;
import com.raffleease.raffleease.Domains.Auth.Services.ILoginService;
import com.raffleease.raffleease.Domains.Token.Services.ITokensCreateService;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Services.IUsersService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.AuthenticationException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LoginServiceImpl implements ILoginService {
    private final AuthenticationManager authenticationManager;
    private final ITokensCreateService tokensCreateService;
    private final ICookiesService cookiesService;
    private final IUsersService usersService;

    @Value("${spring.application.security.jwt.refresh_token_expiration}")
    private Long refreshTokenExpiration;

    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        authenticateCredentials(request.identifier(), request.password());
        User user = usersService.findByIdentifier(request.identifier());
        String accessToken = tokensCreateService.generateAccessToken(user.getId());
        String refreshToken = tokensCreateService.generateRefreshToken(user.getId());
        cookiesService.addCookie(response, "refresh_token", refreshToken, refreshTokenExpiration);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .build();
    }

    private void authenticateCredentials(String identifier, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(identifier, password));
        } catch (org.springframework.security.core.AuthenticationException exp) {
            throw new AuthenticationException("Authentication failed for provided credentials");
        }
    }
}
