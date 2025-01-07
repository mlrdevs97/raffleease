package com.raffleease.raffleease.Domains.Users.Services;

import com.raffleease.raffleease.Domains.Users.Model.User;

public interface IUsersService {
    User findByIdentifier(String identifier);
}
