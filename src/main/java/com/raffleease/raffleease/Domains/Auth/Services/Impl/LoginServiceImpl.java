package com.raffleease.raffleease.Domains.Auth.Services.Impl;

import com.raffleease.raffleease.Domains.Auth.DTOs.AuthRequest;
import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Auth.Services.ILoginService;
import com.raffleease.raffleease.Domains.Token.Services.ITokensCreateService;
import com.raffleease.raffleease.Domains.Token.Services.ITokensManagementService;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Model.UserPrincipal;
import com.raffleease.raffleease.Domains.Users.Services.IUsersService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.AuthenticationException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LoginServiceImpl implements ILoginService {
    private final AuthenticationManager authenticationManager;
    private final ITokensCreateService tokensCreateService;
    private final ITokensManagementService tokensManagementService;
    private final IUsersService usersService;

    public AuthResponse authenticate(AuthRequest request) {
        authenticateCredentials(request.email(), request.password());
        User user = findUser(request.email());
        UserPrincipal principal = UserPrincipal.builder().user(user).build();
        String accessToken = tokensCreateService.generateAccessToken(principal);
        String refreshToken = tokensCreateService.generateRefreshToken(principal);
        tokensManagementService.revokeAllUserTokens(user);
        return AuthResponse.builder()
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .build();
    }

    private User findUser(String identifier) {
        try {
            return usersService.findByIdentifier(identifier);
        } catch (NotFoundException ex) {
            throw new AuthenticationException("Authentication failed for provided credentials");
        }
    }

    private void authenticateCredentials(String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (org.springframework.security.core.AuthenticationException exp) {
            throw new AuthenticationException("Authentication failed for provided credentials");
        }
    }
}
