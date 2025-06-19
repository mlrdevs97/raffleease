package com.raffleease.raffleease.Domains.Users.Services;

import com.raffleease.raffleease.Common.Models.BaseUserData;
import com.raffleease.raffleease.Common.Models.CreateUserData;
import com.raffleease.raffleease.Domains.Users.DTOs.UserResponse;

public interface UserManagementService {
    UserResponse create(Long associationId, CreateUserData userData);
    UserResponse edit(Long associationId, Long userId, BaseUserData userData);
    void disableUserInAssociation(Long associationId, Long userId);
    void enableUserInAssociation(Long associationId, Long userId);
} 