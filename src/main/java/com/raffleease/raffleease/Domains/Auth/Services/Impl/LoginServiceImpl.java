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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LoginServiceImpl implements ILoginService {
    private final AuthenticationManager authenticationManager;
    private final ITokensCreateService tokensCreateService;
    private final ICookiesService cookiesService;
    private final IUsersService usersService;

    @Value("${spring.application.security.jwt.refresh_token_expiration}")
    private Long refreshTokenExpiration;

    @Value("${spring.application.config.is_test}")
    private boolean test;

    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        if (test) log.debug("Initiating login for identifier: {}", request.identifier());
        authenticateCredentials(request.identifier(), request.password());
        User user = usersService.findByIdentifier(request.identifier());
        if (test) log.debug("User authenticated, generating tokens for user: {}", user.getId());
        String accessToken = tokensCreateService.generateAccessToken(user.getId());
        String refreshToken = tokensCreateService.generateRefreshToken(user.getId());
        cookiesService.addCookie(response, "refresh_token", refreshToken, refreshTokenExpiration);
        if (test) log.debug("Tokens generated successfully for user: {}", user.getId());
        return AuthResponse.builder().accessToken(accessToken).build();
    }

    private void authenticateCredentials(String identifier, String password) {
        if (test) log.debug("Authenticating credentials for identifier: {}", identifier);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(identifier, password));
            if (test) log.debug("Authentication successful for identifier: {}", identifier);
        } catch (org.springframework.security.core.AuthenticationException ex) {
            if (test) log.error("Authentication failed for identifier: {}", identifier, ex);
            throw new AuthenticationException("Authentication failed for provided credentials");
        }
    }
}
