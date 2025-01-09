package com.raffleease.raffleease.Domains.Users.Services;

import com.raffleease.raffleease.Domains.Users.Model.User;

public interface IUsersService {
    User findByIdentifier(String identifier);
    User findById(Long id);
    boolean existsById(Long id);
}
