package com.raffleease.raffleease.Domains.Users.Services;

import com.raffleease.raffleease.Common.Models.BaseUserData;
import com.raffleease.raffleease.Common.Models.CreateUserData;
import com.raffleease.raffleease.Domains.Users.DTOs.UserResponse;
import com.raffleease.raffleease.Domains.Users.Model.User;

import java.util.List;

public interface UsersService {

    /**
     * Creates a new user with the provided data and settings.
     * 
     * @param userData the user data containing name, email, and other details
     * @param encodedPassword the pre-encoded password for the user
     * @param isEnabled whether the user account should be enabled upon creation
     * @return the created User entity
     * @throws com.raffleease.raffleease.Common.Exceptions.CustomExceptions.UniqueConstraintViolationException
     *         if email already exists
     */
    User createUser(CreateUserData userData, String encodedPassword, boolean isEnabled);

    /**
     * Updates an existing user's basic information.
     * 
     * @param user the user entity to update
     * @param userData the new user data to apply
     * @return the updated User entity
     */
    User updateUser(User user, BaseUserData userData);

    /**
     * Enables or disables a user account.
     * 
     * @param user the user entity to modify
     * @param enabled true to enable the account, false to disable
     * @return the updated User entity
     */
    User setUserEnabled(User user, boolean enabled);

    /**
     * Updates a user's password with a new encoded password.
     * 
     * @param user the user entity to update
     * @param encodedPassword the new pre-encoded password
     * @return the updated User entity
     */
    User updatePassword(User user, String encodedPassword);

    /**
     * Retrieves a user by ID as a response DTO.
     * 
     * @param userId the ID of the user to retrieve
     * @return the user data as UserResponse DTO
     * @throws com.raffleease.raffleease.Common.Exceptions.CustomExceptions.NotFoundException
     *         if user with the given ID doesn't exist
     */
    UserResponse getUserById(Long userId);

    /**
     * Retrieves all users belonging to a specific association.
     * 
     * @param associationId the ID of the association
     * @return list of users as UserResponse DTOs
     */
    List<UserResponse> getUsersByAssociationId(Long associationId);

    /**
     * Finds a user by their unique identifier (email or username).
     * 
     * @param identifier the email or username to search for
     * @return the User entity if found
     * @throws com.raffleease.raffleease.Common.Exceptions.CustomExceptions.NotFoundException
     *         if no user matches the identifier
     */
    User findByIdentifier(String identifier);

    /**
     * Retrieves a user by their email address.
     * 
     * @param email the email address to search for
     * @return the User entity if found
     * @throws com.raffleease.raffleease.Common.Exceptions.CustomExceptions.NotFoundException
     *         if no user with the given email exists
     */
    User getUserByEmail(String email);

    /**
     * Finds a user by their ID as an entity.
     * 
     * @param id the ID of the user to find
     * @return the User entity if found
     * @throws com.raffleease.raffleease.Common.Exceptions.CustomExceptions.NotFoundException
     *         if user with the given ID doesn't exist
     */
    User findById(Long id);

    /**
     * Checks whether a user exists with the given ID.
     * 
     * @param id the user ID to check
     * @return true if the user exists, false otherwise
     */
    boolean existsById(Long id);

    /**
     * Retrieves the currently authenticated user from the security context.
     * 
     * @return the authenticated User entity
     * @throws com.raffleease.raffleease.Common.Exceptions.CustomExceptions.AuthorizationException
     *         if no user is authenticated or user not found
     */
    User getAuthenticatedUser();
}
