package com.raffleease.raffleease.Domains.Token.Services.Impls;

import com.raffleease.raffleease.Domains.Token.Services.IBlackListService;
import com.raffleease.raffleease.Domains.Token.Services.ITokensQueryService;
import com.raffleease.raffleease.Domains.Token.Services.ITokensValidateService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.AuthorizationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

@RequiredArgsConstructor
@Service
public class TokensValidateServiceImpl implements ITokensValidateService {
    private final ITokensQueryService tokenQueryService;
    private final IBlackListService blackListService;

    @Override
    public void validateToken(String token, UserDetails userDetails) {
        if (!isTokenNonExpired(token)) {
            throw new AuthorizationException("Token expired");
        }

        final String tokenId = tokenQueryService.getTokenId(token);
        if (blackListService.isTokenBlackListed(tokenId)) {
            throw new AuthorizationException("Token revoked");
        }

        final String subject = tokenQueryService.getSubject(token);
        if (!subject.equals(userDetails.getUsername())) {
            throw new AuthorizationException("User name does not match token user name");
        }
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String subject = tokenQueryService.getSubject(token);
        final String tokenId = tokenQueryService.getTokenId(token);
        return (subject.equals(userDetails.getUsername()) &&
                isTokenNonExpired(token) &&
                !blackListService.isTokenBlackListed(tokenId)
        );
    }

    private boolean isTokenNonExpired(String token) {
        return !tokenQueryService.getExpiration(token).before(new Date());
    }
}
