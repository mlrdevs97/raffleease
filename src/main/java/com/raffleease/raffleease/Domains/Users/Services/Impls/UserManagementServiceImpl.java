package com.raffleease.raffleease.Domains.Users.Services.Impls;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Auth.Model.VerificationToken;
import com.raffleease.raffleease.Domains.Auth.Repository.VerificationTokenRepository;
import com.raffleease.raffleease.Domains.Notifications.Services.EmailsService;
import com.raffleease.raffleease.Domains.Users.DTOs.CreateUserData;
import com.raffleease.raffleease.Domains.Users.DTOs.UpdateUserData;
import com.raffleease.raffleease.Domains.Users.DTOs.UserResponse;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Services.UserManagementService;
import com.raffleease.raffleease.Domains.Users.Services.UsersService;
import com.raffleease.raffleease.Common.Configs.CorsProperties;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.DatabaseException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.raffleease.raffleease.Common.Constants.Constants.EMAIL_VERIFICATION_EXPIRATION_MINUTES;
import static com.raffleease.raffleease.Domains.Associations.Model.AssociationRole.MEMBER;

@RequiredArgsConstructor
@Service
public class UserManagementServiceImpl implements UserManagementService {
    private final UsersService usersService;
    private final AssociationsService associationsService;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailsService emailsService;
    private final CorsProperties corsProperties;

    @Transactional
    @Override
    public UserResponse createUserInAssociation(Long associationId, CreateUserData userData) {
        Association association = associationsService.findById(associationId);
        String encodedPassword = passwordEncoder.encode(userData.password());
        User user = usersService.createUser(userData, encodedPassword);
        associationsService.createMembership(association, user, MEMBER);
        handleUserVerification(user);
        return usersService.getUserById(user.getId());
    }

    @Override
    public UserResponse updateUserInAssociation(Long associationId, Long userId, UpdateUserData userData) {
        // Verify association exists and user belongs to it
        associationsService.findById(associationId);
        User updatedUser = usersService.updateUser(userId, userData);
        return usersService.getUserById(updatedUser.getId());
    }

    @Override
    public void disableUserInAssociation(Long associationId, Long userId) {
        // Verify association exists
        associationsService.findById(associationId);
        usersService.disableUser(userId);
    }

    @Override
    public void enableUserInAssociation(Long associationId, Long userId) {
        // Verify association exists
        associationsService.findById(associationId);
        User user = usersService.findById(userId);
        usersService.enableUser(user);
    }

    private void handleUserVerification(User user) {
        VerificationToken verificationToken = createVerificationToken(user);
        String verificationLink = UriComponentsBuilder.fromHttpUrl(corsProperties.getClientAsList().get(0))
                .path("/auth/email-verification")
                .queryParam("token", verificationToken.getToken())
                .build()
                .toUriString();
        emailsService.sendEmailVerificationEmail(user, verificationLink);
    }

    private VerificationToken createVerificationToken(User user) {
        try {
            return verificationTokenRepository.save(VerificationToken.builder()
                    .token(UUID.randomUUID().toString())
                    .user(user)
                    .expiryDate(LocalDateTime.now().plusMinutes(EMAIL_VERIFICATION_EXPIRATION_MINUTES))
                    .build());
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving verification token");
        }
    }
} 