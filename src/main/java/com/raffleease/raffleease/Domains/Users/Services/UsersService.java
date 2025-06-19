package com.raffleease.raffleease.Domains.Users.Services;

import com.raffleease.raffleease.Common.Models.BaseUserData;
import com.raffleease.raffleease.Common.Models.CreateUserData;
import com.raffleease.raffleease.Domains.Users.DTOs.UserResponse;
import com.raffleease.raffleease.Domains.Users.Model.User;

import java.util.List;

public interface UsersService {
    User createUser(CreateUserData userData, String encodedPassword, boolean isEnabled);
    User updateUser(User user, BaseUserData userData);
    User setUserEnabled(User user, boolean enabled);
    User updatePassword(User user, String encodedPassword);
    UserResponse getUserById(Long userId);
    List<UserResponse> getUsersByAssociationId(Long associationId);
    User findByIdentifier(String identifier);
    User getUserByEmail(String email);
    User findById(Long id);
    boolean existsById(Long id);
    User getAuthenticatedUser();
}
