package com.raffleease.raffleease.util;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationMembership;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationRole;
import com.raffleease.raffleease.Domains.Associations.Repository.AssociationsMembershipsRepository;
import com.raffleease.raffleease.Domains.Associations.Repository.AssociationsRepository;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Repository.UsersRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Utility class for creating authenticated test data.
 * Provides common functionality for tests that require authentication.
 */
@Component
@RequiredArgsConstructor
public class AuthTestUtils {
    
    private final UsersRepository usersRepository;
    private final AssociationsRepository associationsRepository;
    private final AssociationsMembershipsRepository membershipsRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    
    private static final String DEFAULT_TEST_PASSWORD = "mySecurePassword#123";

    /**
     * Creates a complete authenticated user setup with association and membership.
     * 
     * @return AuthTestData containing all related entities
     */
    public AuthTestData createAuthenticatedUser() {
        return createAuthenticatedUser(true, AssociationRole.ADMIN);
    }

    /**
     * Creates a complete authenticated user setup with specified parameters.
     * 
     * @param userEnabled whether the user account should be enabled
     * @param role the association role for the user
     * @return AuthTestData containing all related entities
     */
    public AuthTestData createAuthenticatedUser(boolean userEnabled, AssociationRole role) {
        // Create and persist association with address
        Association association = TestDataBuilder.association()
                .name("Test Auth Association")
                .email("auth-test@example.com")
                .phoneNumber("+1234567890")
                .build();
        association = associationsRepository.save(association);

        // Create and persist user with encoded password
        String encodedPassword = passwordEncoder.encode(DEFAULT_TEST_PASSWORD);
        User user = TestDataBuilder.user()
                .userName("authtestuser")
                .email("authtest@example.com")
                .phoneNumber("+1987654321")
                .password(encodedPassword)
                .enabled(userEnabled)
                .build();
        user = usersRepository.save(user);

        // Create and persist membership linking user to association
        AssociationMembership membership = TestDataBuilder.membership()
                .user(user)
                .association(association)
                .role(role)
                .build();
        membership = membershipsRepository.save(membership);

        // Refresh entities to ensure they have all persisted data
        entityManager.flush();
        entityManager.refresh(user);
        entityManager.refresh(association);
        entityManager.refresh(membership);

        // Verify test data is properly set up
        assertThat(user.getId()).isNotNull();
        assertThat(association.getId()).isNotNull();
        assertThat(membership.getId()).isNotNull();
        assertThat(user.isEnabled()).isEqualTo(userEnabled);

        return new AuthTestData(user, association, membership, DEFAULT_TEST_PASSWORD);
    }

    /**
     * Creates an authenticated user with custom credentials.
     * 
     * @param username custom username
     * @param email custom email
     * @param password custom plain text password
     * @return AuthTestData containing all related entities
     */
    public AuthTestData createAuthenticatedUserWithCredentials(String username, String email, String password) {
        // Create and persist association
        Association association = TestDataBuilder.association()
                .name("Custom Auth Association")
                .email("custom-auth@example.com")
                .phoneNumber("+1555666777")
                .build();
        association = associationsRepository.save(association);

        // Create and persist user with custom credentials
        String encodedPassword = passwordEncoder.encode(password);
        User user = TestDataBuilder.user()
                .userName(username)
                .email(email)
                .password(encodedPassword)
                .enabled(true)
                .build();
        user = usersRepository.save(user);

        // Create membership
        AssociationMembership membership = TestDataBuilder.membership()
                .user(user)
                .association(association)
                .role(AssociationRole.ADMIN)
                .build();
        membership = membershipsRepository.save(membership);

        // Refresh entities
        entityManager.flush();
        entityManager.refresh(user);
        entityManager.refresh(association);
        entityManager.refresh(membership);

        return new AuthTestData(user, association, membership, password);
    }

    /**
     * Creates a disabled user account for testing authentication failures.
     * 
     * @return AuthTestData with disabled user
     */
    public AuthTestData createDisabledUser() {
        return createAuthenticatedUser(false, AssociationRole.ADMIN);
    }

    /**
     * Creates a user with MEMBER role instead of ADMIN.
     * 
     * @return AuthTestData with member role
     */
    public AuthTestData createMemberUser() {
        return createAuthenticatedUser(true, AssociationRole.MEMBER);
    }

    /**
     * Record to hold test authentication data for clean organization.
     */
    public record AuthTestData(
            User user, 
            Association association, 
            AssociationMembership membership,
            String plainTextPassword
    ) {}
} 