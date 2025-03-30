package com.raffleease.raffleease.Domains.Users.Repository;

import com.raffleease.raffleease.Domains.Users.Model.User;

import java.util.Optional;

public interface CustomUsersRepository {
    Optional<User> findByIdentifier(String identifier);
}
