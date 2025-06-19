package com.raffleease.raffleease.Domains.Users.Services;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterUserData;
import com.raffleease.raffleease.Domains.Users.DTOs.CreateUserData;
import com.raffleease.raffleease.Domains.Users.DTOs.UpdateUserData;
import com.raffleease.raffleease.Domains.Users.DTOs.UserResponse;
import com.raffleease.raffleease.Domains.Users.Model.User;

import java.util.List;

public interface UsersService {
    User create(RegisterUserData userData, String encodedPassword);
    User createUser(CreateUserData userData, String encodedPassword);
    User updateUser(Long userId, UpdateUserData userData);
    User disableUser(Long userId);
    User enableUser(User user);
    UserResponse getUserById(Long userId);
    List<UserResponse> getUsersByAssociationId(Long associationId);
    User findByIdentifier(String identifier);
    User findById(Long id);
    boolean existsById(Long id);
    User getAuthenticatedUser();
}
