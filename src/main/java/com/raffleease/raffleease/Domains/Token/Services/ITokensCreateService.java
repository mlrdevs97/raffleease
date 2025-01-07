package com.raffleease.raffleease.Domains.Token.Services;

import com.raffleease.raffleease.Domains.Users.Model.UserPrincipal;


public interface ITokensCreateService {
    String generateAccessToken(UserPrincipal principal);
    String generateRefreshToken(UserPrincipal principal);
}
