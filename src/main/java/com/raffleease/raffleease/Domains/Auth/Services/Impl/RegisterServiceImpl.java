package com.raffleease.raffleease.Domains.Auth.Services.Impl;

import com.raffleease.raffleease.Domains.Associations.Services.IAssociationsService;
import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;
import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Auth.Services.IRegisterService;
import com.raffleease.raffleease.Domains.Token.Services.ITokensCreateService;
import com.raffleease.raffleease.Domains.Token.Services.ITokensManagementService;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Model.UserPrincipal;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RegisterServiceImpl implements IRegisterService {
    private final IAssociationsService associationsService;
    private final PasswordEncoder passwordEncoder;
    private final ITokensCreateService tokensCreateService;
    private final ITokensManagementService tokensManagementService;

    @Transactional
    @Override
    public AuthResponse register(AssociationRegister request) {
        User user = associationsService.create(request, passwordEncoder.encode(request.password()));
        UserPrincipal principal = UserPrincipal.builder().user(user).build();
        String refreshToken = tokensCreateService.generateRefreshToken(principal);
        String accessToken = tokensCreateService.generateAccessToken(principal);
        tokensManagementService.revokeAllUserTokens(user);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
