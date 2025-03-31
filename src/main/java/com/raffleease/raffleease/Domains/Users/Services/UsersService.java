package com.raffleease.raffleease.Domains.Users.Services;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterUserData;
import com.raffleease.raffleease.Domains.Users.Model.User;

public interface UsersService {
    User create(RegisterUserData userData, String encodedPassword);
    User findByIdentifier(String identifier);
    User findById(Long id);
    boolean existsById(Long id);
}
