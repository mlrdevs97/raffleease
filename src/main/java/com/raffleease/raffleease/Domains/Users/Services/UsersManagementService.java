package com.raffleease.raffleease.Domains.Users.Services;

import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.AuthenticationException;
import com.raffleease.raffleease.Common.Models.BaseUserData;
import com.raffleease.raffleease.Common.Models.CreateUserData;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationRole;
import com.raffleease.raffleease.Domains.Auth.DTOs.EditPasswordRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.CreateUserRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.UserResponse;

public interface UsersManagementService {
    /**
     * Create a new user account.
     *
     * @param associationId The ID of the association
     * @param request The request containing user data and role
     * @return The created user response
     */
    UserResponse create(Long associationId, CreateUserRequest request);

    /**
     * Edit a user account.
     *
     * @param associationId The ID of the association
     * @param userId The ID of the user to edit
     * @param userData The data to edit the user with
     * @return The updated user response
     */
    UserResponse edit(Long associationId, Long userId, BaseUserData userData);

    /**
     * Disable a user account. Disabled accounts cannot login to the application.
     *
     * @param associationId The ID of the association
     * @param userId The ID of the user to disable
     */
    void disableUserInAssociation(Long associationId, Long userId);
    
    /**
     * Enable a user account. Enabled accounts can login to the application.
     *
     * @param associationId The ID of the association
     * @param userId The ID of the user to enable
     */
    void enableUserInAssociation(Long associationId, Long userId);

    /**
     * Edit password for an authenticated user using their current password.
     *
     * @param request The request containing current password, new password, and confirmation
     * @throws AuthenticationException If the current password is incorrect
     */
    void editPassword(EditPasswordRequest request);
} 