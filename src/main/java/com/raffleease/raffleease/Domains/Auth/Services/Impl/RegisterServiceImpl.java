package com.raffleease.raffleease.Domains.Auth.Services.Impl;

import com.raffleease.raffleease.Domains.Associations.Services.IAssociationsService;
import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;
import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Auth.Services.ICookiesService;
import com.raffleease.raffleease.Domains.Auth.Services.IRegisterService;
import com.raffleease.raffleease.Domains.Token.Services.ITokensCreateService;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Model.UserPrincipal;
import jakarta.servlet.http.HttpServletResponse;
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
    private final ICookiesService cookiesService;

    @Transactional
    @Override
    public AuthResponse register(AssociationRegister request, HttpServletResponse response) {
        User user = associationsService.create(request, passwordEncoder.encode(request.password()));
        String refreshToken = tokensCreateService.generateRefreshToken(user);
        String accessToken = tokensCreateService.generateAccessToken(user);
        cookiesService.addCookie(response, "refresh_token", refreshToken, 6048000);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
