package com.raffleease.raffleease.Domains.Users.Services.Impls;

import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.AuthenticationException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.AuthorizationException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.BusinessException;
import com.raffleease.raffleease.Common.Models.BaseUserData;
import com.raffleease.raffleease.Common.Models.CreateUserData;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsMembershipService;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Auth.DTOs.EditPasswordRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.CreateUserRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.UserResponse;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Services.UsersManagementService;
import com.raffleease.raffleease.Domains.Users.Services.UsersService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.raffleease.raffleease.Domains.Associations.Model.AssociationRole.MEMBER;

@Slf4j
@RequiredArgsConstructor
@Service
public class UsersManagementServiceImpl implements UsersManagementService {
    private final UsersService usersService;
    private final AssociationsService associationsService;
    private final AssociationsMembershipService membershipService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public UserResponse create(Long associationId, CreateUserRequest request) {
        String encodedPassword = passwordEncoder.encode(request.userData().getPassword());
        User user = usersService.createUser(request.userData(), encodedPassword, true);
        Association association = associationsService.findById(associationId);
        associationsService.createMembership(association, user, request.role());
        return usersService.getUserById(user.getId());
    }

    @Transactional
    @Override
    public UserResponse edit(Long associationId, Long userId, BaseUserData userData) {
        Association association = associationsService.findById(associationId);
        User user = usersService.findById(userId);
        validateMembership(association, user);
        User updatedUser = usersService.updateUser(user, userData);
        return usersService.getUserById(updatedUser.getId());
    }

    @Transactional
    @Override
    public void disableUserInAssociation(Long associationId, Long userId) {
        Association association = associationsService.findById(associationId);
        User user = usersService.findById(userId);
        validateMembership(association, user);
        usersService.setUserEnabled(user, false);
    }

    @Transactional
    @Override
    public void enableUserInAssociation(Long associationId, Long userId) {
        Association association = associationsService.findById(associationId);
        User user = usersService.findById(userId);
        validateMembership(association, user);
        usersService.setUserEnabled(user, true);
    }

    @Transactional
    @Override
    public void editPassword(EditPasswordRequest request) {
        User authenticatedUser = usersService.getAuthenticatedUser();

        // Verify current password
        if (!passwordEncoder.matches(request.currentPassword(), authenticatedUser.getPassword())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        // Check if new password is different from current password
        if (passwordEncoder.matches(request.password(), authenticatedUser.getPassword())) {
            throw new AuthenticationException("New password must be different from current password");
        }

        // Update password
        String encodedNewPassword = passwordEncoder.encode(request.password());
        usersService.updatePassword(authenticatedUser, encodedNewPassword);

        log.info("Password updated successfully for user: {}", authenticatedUser.getUserName());
    }

    private void validateMembership(Association association, User user) {
        try {
            membershipService.validateIsMember(association, user);
        } catch (Exception ex) {
            if (ex instanceof AuthorizationException) {
                throw new BusinessException(ex.getMessage());
            }
            throw ex;
        }
    }
} 