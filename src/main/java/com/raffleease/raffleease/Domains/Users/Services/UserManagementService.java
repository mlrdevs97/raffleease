package com.raffleease.raffleease.Domains.Users.Services;

import com.raffleease.raffleease.Domains.Users.DTOs.CreateUserData;
import com.raffleease.raffleease.Domains.Users.DTOs.UpdateUserData;
import com.raffleease.raffleease.Domains.Users.DTOs.UserResponse;

public interface UserManagementService {
    UserResponse createUserInAssociation(Long associationId, CreateUserData userData);
    UserResponse updateUserInAssociation(Long associationId, Long userId, UpdateUserData userData);
    void disableUserInAssociation(Long associationId, Long userId);
    void enableUserInAssociation(Long associationId, Long userId);
} 