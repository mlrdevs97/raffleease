package com.raffleease.raffleease.Domains.Users.Services.Impls;

import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.AuthorizationException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.BusinessException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.PasswordResetException;
import com.raffleease.raffleease.Common.Models.UserBaseDTO;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsMembershipService;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Auth.DTOs.EditPasswordRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.CreateUserRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.UpdatePhoneNumberRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.UserResponse;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Model.UserPhoneNumber;
import com.raffleease.raffleease.Domains.Users.Services.UsersManagementService;
import com.raffleease.raffleease.Domains.Users.Services.UsersService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.raffleease.raffleease.Domains.Associations.Model.AssociationRole.ADMIN;
import static com.raffleease.raffleease.Common.Exceptions.ErrorCodes.CURRENT_PASSWORD_INCORRECT;
import static com.raffleease.raffleease.Common.Exceptions.ErrorCodes.PASSWORD_SAME_AS_CURRENT;

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
        if (request.role() == ADMIN) {
            throw new AuthorizationException("Administrators cannot create other administrator accounts");
        }
        String encodedPassword = passwordEncoder.encode(request.userData().getPassword());
        User user = usersService.createUser(request.userData(), encodedPassword, true);
        Association association = associationsService.findById(associationId);
        associationsService.createMembership(association, user, request.role());
        return usersService.getUserResponseById(user.getId());
    }

    @Transactional
    @Override
    public UserResponse edit(Long userId, UserBaseDTO userData) {
        User user = usersService.findById(userId);
        User updatedUser = usersService.updateUser(user, userData);
        return usersService.getUserResponseById(updatedUser.getId());
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

        if (!passwordEncoder.matches(request.currentPassword(), authenticatedUser.getPassword())) {
            throw new PasswordResetException("Current password is incorrect", CURRENT_PASSWORD_INCORRECT);
        }

        if (passwordEncoder.matches(request.password(), authenticatedUser.getPassword())) {
            throw new BusinessException("New password must be different from current password", PASSWORD_SAME_AS_CURRENT);
        }

        String encodedNewPassword = passwordEncoder.encode(request.password());
        usersService.updatePassword(authenticatedUser, encodedNewPassword);

        log.info("Password updated successfully for user: {}", authenticatedUser.getUserName());
    }

    @Transactional
    @Override
    public UserResponse updatePhoneNumber(Long associationId, Long userId, UpdatePhoneNumberRequest request) {
        Association association = associationsService.findById(associationId);
        User user = usersService.findById(userId);
        validateMembership(association, user);
        UserPhoneNumber phoneNumber = UserPhoneNumber.builder()
            .prefix(request.phoneNumber().prefix())
            .nationalNumber(request.phoneNumber().nationalNumber())
            .build();
        user.setPhoneNumber(phoneNumber);
        User updatedUser = usersService.save(user);
        
        log.info("Phone number updated successfully for user: {}", user.getUserName());
        return usersService.getUserResponseById(updatedUser.getId());
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