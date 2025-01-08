package com.raffleease.raffleease.Domains.Token.Services;

import com.raffleease.raffleease.Domains.Users.Model.User;


public interface ITokensCreateService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
}
