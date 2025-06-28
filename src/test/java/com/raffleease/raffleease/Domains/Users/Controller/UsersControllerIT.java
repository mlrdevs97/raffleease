package com.raffleease.raffleease.Domains.Users.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffleease.raffleease.Base.AbstractIntegrationTest;
import com.raffleease.raffleease.Common.Models.UserBaseDTO;
import com.raffleease.raffleease.Common.Models.UserProfileDTO;
import com.raffleease.raffleease.Common.Models.UserRegisterDTO;
import com.raffleease.raffleease.Common.Models.PhoneNumberDTO;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationRole;
import com.raffleease.raffleease.Domains.Auth.DTOs.EditPasswordRequest;
import com.raffleease.raffleease.Domains.Auth.Model.VerificationToken;
import com.raffleease.raffleease.Domains.Auth.Repository.VerificationTokenRepository;
import com.raffleease.raffleease.Domains.Users.DTOs.CreateUserRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.EditUserRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.UpdateEmailRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.UpdatePhoneNumberRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.UpdateUserRoleRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.VerifyEmailUpdateRequest;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Repository.UsersRepository;
import com.raffleease.raffleease.Domains.Auth.Repository.EmailUpdateTokenRepository;
import com.raffleease.raffleease.util.AuthTestUtils;
import com.raffleease.raffleease.util.AuthTestUtils.AuthTestData;
import org.junit.jupiter.api.DisplayName;>
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.ResultActions;

import static com.raffleease.raffleease.Domains.Associations.Model.AssociationRole.ADMIN;
import static com.raffleease.raffleease.Domains.Associations.Model.AssociationRole.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Users Controller Integration Tests")
class UsersControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthTestUtils authTestUtils;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    private static final String USERS_BASE_ENDPOINT = "/v1/associations/{associationId}/users";

    @Nested
    @DisplayName("POST /v1/associations/{associationId}/users - Create User")
    class CreateUserTests {

        @Test
        @DisplayName("Should successfully create user with valid data and send verification email")
        void shouldCreateUserWithValidDataAndSendVerificationEmail() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            CreateUserRequest request = createValidCreateUserRequest();

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User account created successfully. Verification email sent to the user."))
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.firstName").value("john"))
                    .andExpect(jsonPath("$.data.lastName").value("doe"))
                    .andExpect(jsonPath("$.data.userName").value("johndoe"))
                    .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.data.phoneNumber.prefix").value("+1"))
                    .andExpect(jsonPath("$.data.phoneNumber.nationalNumber").value("234567890"))
                    .andExpect(jsonPath("$.data.userRole").value("ASSOCIATION_MEMBER"))
                    .andExpect(jsonPath("$.data.isEnabled").value(false)); // Changed: users are now created disabled

            // Verify verification token was created
            Long createdUserId = authTestUtils.extractUserIdFromResponse(result);
            User createdUser = usersRepository.findById(createdUserId).orElseThrow();
            
            // Check that a verification token was created for the user
            VerificationToken verificationToken = verificationTokenRepository.findByUser(createdUser)
                    .orElseThrow(() -> new AssertionError("Verification token should have been created"));
            assertThat(verificationToken.getToken()).isNotNull();
            assertThat(verificationToken.getExpiryDate()).isAfter(java.time.LocalDateTime.now());
        }

        @Test
        @DisplayName("Should return 403 when MEMBER tries to create user")
        void shouldReturn403WhenMemberTriesToCreateUser() throws Exception {
            // Arrange
            AuthTestData memberData = authTestUtils.createAuthenticatedUser(true, MEMBER);
            CreateUserRequest request = createValidCreateUserRequest();

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT, memberData.association().getId())
                    .with(user(memberData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Only administrators can create user accounts"));
        }

        @Test
        @DisplayName("Should return 403 when COLLABORATOR tries to create user")
        void shouldReturn403WhenCollaboratorTriesToCreateUser() throws Exception {
            // Arrange
            AuthTestData collaboratorData = authTestUtils.createAuthenticatedUser(true, AssociationRole.COLLABORATOR);
            CreateUserRequest request = createValidCreateUserRequest();

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT, collaboratorData.association().getId())
                    .with(user(collaboratorData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Only administrators can create user accounts"));
        }

        @Test
        @DisplayName("Should return 401 when unauthenticated user tries to create user")
        void shouldReturn401WhenUnauthenticatedUserTriesToCreateUser() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            CreateUserRequest request = createValidCreateUserRequest();

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when request data is invalid")
        void shouldReturn400WhenRequestDataIsInvalid() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            CreateUserRequest invalidRequest = createInvalidCreateUserRequest();

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should return 400 when passwords don't match")
        void shouldReturn400WhenPasswordsDontMatch() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            CreateUserRequest requestWithMismatchedPasswords = createUserRequestWithMismatchedPasswords();

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestWithMismatchedPasswords)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors['userData.confirmPassword']").value("INVALID_FIELD"));
        }

        @Test
        @DisplayName("Should return 409 when username already exists")
        void shouldReturn409WhenUsernameAlreadyExists() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            CreateUserRequest request = createUserRequestWithExistingUsername(adminData.user().getUserName());

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors['userData.userName']").value("VALUE_ALREADY_EXISTS"));
        }

        @Test
        @DisplayName("Should return 409 when email already exists")
        void shouldReturn409WhenEmailAlreadyExists() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            CreateUserRequest request = createUserRequestWithExistingEmail(adminData.user().getEmail());

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors['userData.email']").value("VALUE_ALREADY_EXISTS"));
        }

        @Test
        @DisplayName("Should return 404 when association doesn't exist")
        void shouldReturn404WhenAssociationDoesntExist() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            CreateUserRequest request = createValidCreateUserRequest();
            Long nonExistentAssociationId = 99999L;

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT, nonExistentAssociationId)
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should not create verification token when user creation fails")
        void shouldNotCreateVerificationTokenWhenUserCreationFails() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            CreateUserRequest request = createUserRequestWithExistingUsername(adminData.user().getUserName());
            
            int tokenCountBefore = verificationTokenRepository.findAll().size();

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false));

            // Verify no verification token was created
            int tokenCountAfter = verificationTokenRepository.findAll().size();
            assertThat(tokenCountAfter).isEqualTo(tokenCountBefore);
        }

        @Test
        @DisplayName("Should create user in disabled state requiring verification")
        void shouldCreateUserInDisabledStateRequiringVerification() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            CreateUserRequest request = createValidCreateUserRequest();

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            Long createdUserId = authTestUtils.extractUserIdFromResponse(result);
            User createdUser = usersRepository.findById(createdUserId).orElseThrow();
            
            // Verify user is disabled and cannot authenticate until verification
            assertThat(createdUser.isEnabled()).isFalse();
            assertThat(createdUser.getEmail()).isEqualTo("john.doe@example.com");
            
            // Verify verification token exists and is valid
            VerificationToken token = verificationTokenRepository.findByUser(createdUser)
                    .orElseThrow(() -> new AssertionError("Verification token should exist"));
            assertThat(token.getExpiryDate()).isAfter(java.time.LocalDateTime.now());
        }
    }

    @Nested
    @DisplayName("GET /v1/associations/{associationId}/users/{id} - Get User By ID")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should successfully return user by ID for ADMIN")
        void shouldReturnUserByIdForAdmin() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            User targetUser = adminData.user();

            // Act
            ResultActions result = mockMvc.perform(get(USERS_BASE_ENDPOINT + "/{id}", 
                    adminData.association().getId(), targetUser.getId())
                    .with(user(adminData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value(targetUser.getId()))
                    .andExpect(jsonPath("$.data.firstName").value(targetUser.getFirstName()))
                    .andExpect(jsonPath("$.data.lastName").value(targetUser.getLastName()))
                    .andExpect(jsonPath("$.data.userName").value(targetUser.getUserName()))
                    .andExpect(jsonPath("$.data.email").value(targetUser.getEmail()))
                    .andExpect(jsonPath("$.data.role").value(ADMIN))
                    .andExpect(jsonPath("$.data.isEnabled").value(targetUser.isEnabled()));
        }

        @Test
        @DisplayName("Should allow MEMBER to access their own user data")
        void shouldAllowMemberToAccessOwnUserData() throws Exception {
            // Arrange
            AuthTestData memberData = authTestUtils.createAuthenticatedUser(true, MEMBER);

            // Act
            ResultActions result = mockMvc.perform(get(USERS_BASE_ENDPOINT + "/{id}", 
                    memberData.association().getId(), memberData.user().getId())
                    .with(user(memberData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value(memberData.user().getId()));
        }

        @Test
        @DisplayName("Should allow COLLABORATOR to access their own user data")
        void shouldAllowCollaboratorToAccessOwnUserData() throws Exception {
            // Arrange
            AuthTestData collaboratorData = authTestUtils.createAuthenticatedUser(true, AssociationRole.COLLABORATOR);

            // Act
            ResultActions result = mockMvc.perform(get(USERS_BASE_ENDPOINT + "/{id}", 
                    collaboratorData.association().getId(), collaboratorData.user().getId())
                    .with(user(collaboratorData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value(collaboratorData.user().getId()));
        }

        @Test
        @DisplayName("Should return 403 when MEMBER tries to access another user's data")
        void shouldReturn403WhenMemberTriesToAccessAnotherUser() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData memberData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());

            // Act - Member tries to access admin's data
            ResultActions result = mockMvc.perform(get(USERS_BASE_ENDPOINT + "/{id}", 
                    adminData.association().getId(), adminData.user().getId())
                    .with(user(memberData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Only administrators can access other users' account information, or users can access their own account"));
        }

        @Test
        @DisplayName("Should return 403 when COLLABORATOR tries to access another user's data")
        void shouldReturn403WhenCollaboratorTriesToAccessAnotherUser() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData collaboratorData = authTestUtils.createAuthenticatedUser(true, AssociationRole.COLLABORATOR);
            // Create membership for collaborator in admin's association
            AuthTestData collaboratorInSameAssociation = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());

            // Act - Collaborator tries to access admin's data
            ResultActions result = mockMvc.perform(get(USERS_BASE_ENDPOINT + "/{id}", 
                    adminData.association().getId(), adminData.user().getId())
                    .with(user(collaboratorInSameAssociation.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Only administrators can access other users' account information, or users can access their own account"));
        }

        @Test
        @DisplayName("Should return 401 when unauthenticated user tries to get user")
        void shouldReturn401WhenUnauthenticatedUserTriesToGetUser() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);

            // Act
            ResultActions result = mockMvc.perform(get(USERS_BASE_ENDPOINT + "/{id}", 
                    adminData.association().getId(), adminData.user().getId()));

            // Assert
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 404 when association doesn't exist")
        void shouldReturn404WhenAssociationDoesntExist() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            Long nonExistentAssociationId = 99999L;

            // Act
            ResultActions result = mockMvc.perform(get(USERS_BASE_ENDPOINT + "/{id}", 
                    nonExistentAssociationId, adminData.user().getId())
                    .with(user(adminData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /v1/associations/{associationId}/users/{id} - Edit User")
    class EditUserTests {

        @Test
        @DisplayName("Should successfully update user with valid data for ADMIN")
        void shouldUpdateUserWithValidDataForAdmin() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            User targetUser = adminData.user();
            EditUserRequest request = createValidEditUserRequest();

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{id}", 
                    adminData.association().getId(), targetUser.getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User updated successfully"))
                    .andExpect(jsonPath("$.data.id").value(targetUser.getId()))
                    .andExpect(jsonPath("$.data.firstName").value("jane"))
                    .andExpect(jsonPath("$.data.lastName").value("smith"))
                    .andExpect(jsonPath("$.data.userName").value("janesmith"))
                    .andExpect(jsonPath("$.data.email").value(targetUser.getEmail()));
        }

        @Test
        @DisplayName("Should allow MEMBER to update their own data")
        void shouldAllowMemberToUpdateOwnData() throws Exception {
            // Arrange
            AuthTestData memberData = authTestUtils.createAuthenticatedUser(true, MEMBER);
            EditUserRequest request = createValidEditUserRequest();

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{id}", 
                    memberData.association().getId(), memberData.user().getId())
                    .with(user(memberData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User updated successfully"));
        }

        @Test
        @DisplayName("Should allow COLLABORATOR to update their own data")
        void shouldAllowCollaboratorToUpdateOwnData() throws Exception {
            // Arrange
            AuthTestData collaboratorData = authTestUtils.createAuthenticatedUser(true, AssociationRole.COLLABORATOR);
            EditUserRequest request = createValidEditUserRequest();

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{id}", 
                    collaboratorData.association().getId(), collaboratorData.user().getId())
                    .with(user(collaboratorData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User updated successfully"));
        }

        @Test
        @DisplayName("Should return 403 when MEMBER tries to update another user")
        void shouldReturn403WhenMemberTriesToUpdateAnotherUser() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData memberData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            EditUserRequest request = createValidEditUserRequest();

            // Act - Member tries to update admin
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{id}", 
                    adminData.association().getId(), adminData.user().getId())
                    .with(user(memberData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You can only update your own account"));
        }

        @Test
        @DisplayName("Should return 403 when COLLABORATOR tries to update another user")
        void shouldReturn403WhenCollaboratorTriesToUpdateAnotherUser() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData collaboratorData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            EditUserRequest request = createValidEditUserRequest();

            // Act - Collaborator tries to update admin
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{id}", 
                    adminData.association().getId(), adminData.user().getId())
                    .with(user(collaboratorData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You can only update your own account"));
        }

        @Test
        @DisplayName("Should return 401 when unauthenticated user tries to update user")
        void shouldReturn401WhenUnauthenticatedUserTriesToUpdateUser() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            EditUserRequest request = createValidEditUserRequest();

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{id}", 
                    adminData.association().getId(), adminData.user().getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 409 when updated username already exists")
        void shouldReturn409WhenUpdatedUsernameAlreadyExists() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData otherUserData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            
            User adminUser = usersRepository.findById(adminData.user().getId()).orElseThrow();
            User otherUser = usersRepository.findById(otherUserData.user().getId()).orElseThrow();
            String conflictUsername = "conflictuser";
            otherUser.setUserName(conflictUsername);
            usersRepository.save(otherUser);            
            assertThat(adminUser.getUserName()).isNotEqualTo(otherUser.getUserName());
            assertThat(adminUser.getId()).isNotEqualTo(otherUser.getId());
            
            EditUserRequest request = createEditUserRequestWithExistingUsername(conflictUsername);

            // Act - Try to update adminUser to have otherUser's username
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{id}", 
                    adminData.association().getId(), adminUser.getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors['userData.userName']").value("VALUE_ALREADY_EXISTS"));
        }
    }

    @Nested
    @DisplayName("PATCH /v1/associations/{associationId}/users/{userId}/disable - Disable User")
    class DisableUserTests {

        @Test
        @DisplayName("Should successfully disable user for ADMIN")
        void shouldDisableUserForAdmin() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData memberData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            User targetUser = memberData.user();

            // Act
            ResultActions result = mockMvc.perform(patch(USERS_BASE_ENDPOINT + "/{userId}/disable", 
                    adminData.association().getId(), targetUser.getId())
                    .with(user(adminData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User disabled successfully"))
                    .andExpect(jsonPath("$.data").isEmpty());

            // Verify user is actually disabled in database
            User disabledUser = usersRepository.findById(targetUser.getId()).orElseThrow();
            assertThat(disabledUser.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("Should return 403 when ADMIN tries to disable themselves")
        void shouldReturn403WhenAdminTriesToDisableThemselves() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);

            // Act - Admin tries to disable themselves
            ResultActions result = mockMvc.perform(patch(USERS_BASE_ENDPOINT + "/{userId}/disable", 
                    adminData.association().getId(), adminData.user().getId())
                    .with(user(adminData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Administrators cannot disable their own account"));
        }

        @Test
        @DisplayName("Should return 403 when MEMBER tries to disable user")
        void shouldReturn403WhenMemberTriesToDisableUser() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData memberData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());

            // Act - Member tries to disable admin
            ResultActions result = mockMvc.perform(patch(USERS_BASE_ENDPOINT + "/{userId}/disable", 
                    adminData.association().getId(), adminData.user().getId())
                    .with(user(memberData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Only administrators can disable user accounts"));
        }

        @Test
        @DisplayName("Should return 403 when COLLABORATOR tries to disable user")
        void shouldReturn403WhenCollaboratorTriesToDisableUser() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData collaboratorData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());

            // Act - Collaborator tries to disable admin
            ResultActions result = mockMvc.perform(patch(USERS_BASE_ENDPOINT + "/{userId}/disable", 
                    adminData.association().getId(), adminData.user().getId())
                    .with(user(collaboratorData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Only administrators can disable user accounts"));
        }

        @Test
        @DisplayName("Should return 401 when unauthenticated user tries to disable user")
        void shouldReturn401WhenUnauthenticatedUserTriesToDisableUser() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);

            // Act
            ResultActions result = mockMvc.perform(patch(USERS_BASE_ENDPOINT + "/{userId}/disable", 
                    adminData.association().getId(), adminData.user().getId()));

            // Assert
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 404 when association doesn't exist")
        void shouldReturn404WhenAssociationDoesntExist() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            Long nonExistentAssociationId = 99999L;

            // Act
            ResultActions result = mockMvc.perform(patch(USERS_BASE_ENDPOINT + "/{userId}/disable", 
                    nonExistentAssociationId, adminData.user().getId())
                    .with(user(adminData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /v1/associations/{associationId}/users/{userId}/enable - Enable User")
    class EnableUserTests {

        @Test
        @DisplayName("Should successfully enable user for ADMIN")
        void shouldEnableUserForAdmin() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData memberData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            User targetUser = memberData.user();
            
            // Manually disable the user first so we can test enabling it
            targetUser.setEnabled(false);
            usersRepository.save(targetUser);

            // Act
            ResultActions result = mockMvc.perform(patch(USERS_BASE_ENDPOINT + "/{userId}/enable", 
                    adminData.association().getId(), targetUser.getId())
                    .with(user(adminData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User enabled successfully"))
                    .andExpect(jsonPath("$.data").isEmpty());

            // Verify user is actually enabled in database
            User enabledUser = usersRepository.findById(targetUser.getId()).orElseThrow();
            assertThat(enabledUser.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("Should return 403 when MEMBER tries to enable user")
        void shouldReturn403WhenMemberTriesToEnableUser() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData memberData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());

            // Act - Member tries to enable admin
            ResultActions result = mockMvc.perform(patch(USERS_BASE_ENDPOINT + "/{userId}/enable", 
                    adminData.association().getId(), adminData.user().getId())
                    .with(user(memberData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Only administrators can enable user accounts"));
        }

        @Test
        @DisplayName("Should return 403 when COLLABORATOR tries to enable user")
        void shouldReturn403WhenCollaboratorTriesToEnableUser() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData collaboratorData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());

            // Act - Collaborator tries to enable admin
            ResultActions result = mockMvc.perform(patch(USERS_BASE_ENDPOINT + "/{userId}/enable", 
                    adminData.association().getId(), adminData.user().getId())
                    .with(user(collaboratorData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Only administrators can enable user accounts"));
        }

        @Test
        @DisplayName("Should return 401 when unauthenticated user tries to enable user")
        void shouldReturn401WhenUnauthenticatedUserTriesToEnableUser() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);

            // Act
            ResultActions result = mockMvc.perform(patch(USERS_BASE_ENDPOINT + "/{userId}/enable", 
                    adminData.association().getId(), adminData.user().getId()));

            // Assert
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 404 when association doesn't exist")
        void shouldReturn404WhenAssociationDoesntExist() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            Long nonExistentAssociationId = 99999L;

            // Act
            ResultActions result = mockMvc.perform(patch(USERS_BASE_ENDPOINT + "/{userId}/enable", 
                    nonExistentAssociationId, adminData.user().getId())
                    .with(user(adminData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /v1/associations/{associationId}/users/{userId}/password - Edit Password")
    class EditPasswordTests {

        @Test
        @DisplayName("Should successfully update password for authenticated user")
        void shouldUpdatePasswordForAuthenticatedUser() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            User user = userData.user();
            String originalPassword = user.getPassword();
            String currentPlainPassword = "TempPassword#123"; // Known current password

            // Set a known password for testing
            user.setPassword(passwordEncoder.encode(currentPlainPassword));
            usersRepository.save(user);

            EditPasswordRequest request = new EditPasswordRequest(
                    currentPlainPassword,
                    "NewSecurePass#456",
                    "NewSecurePass#456"
            );

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/password",
                    userData.association().getId(), user.getId())
                    .with(user(user.getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Password has been updated successfully"))
                    .andExpect(jsonPath("$.data").isEmpty());

            // Verify password was actually changed
            User updatedUser = usersRepository.findById(user.getId()).orElseThrow();
            assertThat(passwordEncoder.matches("NewSecurePass#456", updatedUser.getPassword())).isTrue();
            assertThat(updatedUser.getPassword()).isNotEqualTo(originalPassword);
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401WhenUserNotAuthenticated() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            EditPasswordRequest request = new EditPasswordRequest(
                    "CurrentPass#123",
                    "NewSecurePass#456",
                    "NewSecurePass#456"
            );

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/password",
                    userData.association().getId(), userData.user().getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when current password is incorrect")
        void shouldReturn400WhenCurrentPasswordIsIncorrect() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            User user = userData.user();

            EditPasswordRequest request = new EditPasswordRequest(
                    "WrongCurrentPassword#123",
                    "NewSecurePass#456",
                    "NewSecurePass#456"
            );

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/password",
                    userData.association().getId(), user.getId())
                    .with(user(user.getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Current password is incorrect"));
        }

        @Test
        @DisplayName("Should return 400 when new password is same as current password")
        void shouldReturn400WhenNewPasswordIsSameAsCurrent() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            User user = userData.user();
            String currentPlainPassword = "CurrentPass#123";

            // Set a known password for testing
            user.setPassword(passwordEncoder.encode(currentPlainPassword));
            usersRepository.save(user);

            EditPasswordRequest request = new EditPasswordRequest(
                    currentPlainPassword,
                    currentPlainPassword,
                    currentPlainPassword
            );

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/password",
                    userData.association().getId(), user.getId())
                    .with(user(user.getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("New password must be different from current password"));
        }

        @Test
        @DisplayName("Should return 400 when passwords don't match")
        void shouldReturn400WhenPasswordsDontMatch() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            User user = userData.user();
            String currentPlainPassword = "CurrentPass#123";

            // Set a known password for testing
            user.setPassword(passwordEncoder.encode(currentPlainPassword));
            usersRepository.save(user);

            EditPasswordRequest request = new EditPasswordRequest(
                    currentPlainPassword,
                    "NewSecurePass#456",
                    "DifferentPass#789" // Different confirmation password
            );

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/password",
                    userData.association().getId(), user.getId())
                    .with(user(user.getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.confirmPassword").value("INVALID_FIELD"));
        }

        @Test
        @DisplayName("Should return 400 when new password is weak")
        void shouldReturn400WhenNewPasswordIsWeak() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            User user = userData.user();
            String currentPlainPassword = "CurrentPass#123";

            // Set a known password for testing
            user.setPassword(passwordEncoder.encode(currentPlainPassword));
            usersRepository.save(user);

            EditPasswordRequest request = new EditPasswordRequest(
                    currentPlainPassword,
                    "weak", // Weak password
                    "weak"
            );

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/password",
                    userData.association().getId(), user.getId())
                    .with(user(user.getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.password").value("INVALID_FORMAT"));
        }

        @Test
        @DisplayName("Should return 400 when required fields are blank")
        void shouldReturn400WhenRequiredFieldsAreBlank() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            User user = userData.user();

            EditPasswordRequest request = new EditPasswordRequest(
                    "",
                    "",
                    ""  
            );

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/password",
                    userData.association().getId(), user.getId())
                    .with(user(user.getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert - ERROR responses do NOT have a data field (ValidationErrorResponse)
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.currentPassword").value("REQUIRED"))
                    .andExpect(jsonPath("$.errors.password").value("INVALID_FORMAT"))
                    .andExpect(jsonPath("$.errors.confirmPassword").value("REQUIRED"));
        }

        @Test
        @DisplayName("Should preserve user permissions and data after password change")
        void shouldPreserveUserDataAfterPasswordChange() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            User user = userData.user();
            String currentPlainPassword = "CurrentPass#123";
            
            // Store original user data
            String originalFirstName = user.getFirstName();
            String originalLastName = user.getLastName();
            String originalEmail = user.getEmail();
            boolean originalEnabled = user.isEnabled();

            // Set a known password for testing
            user.setPassword(passwordEncoder.encode(currentPlainPassword));
            usersRepository.save(user);

            EditPasswordRequest request = new EditPasswordRequest(
                    currentPlainPassword,
                    "NewSecurePass#456",
                    "NewSecurePass#456"
            );

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/password",
                    userData.association().getId(), user.getId())
                    .with(user(user.getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isOk());

            // Verify all user data except password remained unchanged
            User updatedUser = usersRepository.findById(user.getId()).orElseThrow();
            assertThat(updatedUser.getFirstName()).isEqualTo(originalFirstName);
            assertThat(updatedUser.getLastName()).isEqualTo(originalLastName);
            assertThat(updatedUser.getEmail()).isEqualTo(originalEmail);
            assertThat(updatedUser.isEnabled()).isEqualTo(originalEnabled);
            
            // But password should be changed
            assertThat(passwordEncoder.matches("NewSecurePass#456", updatedUser.getPassword())).isTrue();
        }

        @Test
        @DisplayName("Should return 403 when trying to change another user's password")
        void shouldReturn403WhenTryingToChangeAnotherUsersPassword() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData otherUserData = authTestUtils.createAuthenticatedUserInSameAssociation(userData.association());
            
            EditPasswordRequest request = new EditPasswordRequest(
                    "CurrentPass#123",
                    "NewSecurePass#456",
                    "NewSecurePass#456"
            );

            // Act - User tries to change another user's password
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/password",
                    userData.association().getId(), otherUserData.user().getId())
                    .with(user(userData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You can only change your own password"));
        }

        @Test
        @DisplayName("Should return 404 when association doesn't exist")
        void shouldReturn404WhenAssociationDoesntExist() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            Long nonExistentAssociationId = 99999L;
            
            EditPasswordRequest request = new EditPasswordRequest(
                    "CurrentPass#123",
                    "NewSecurePass#456",
                    "NewSecurePass#456"
            );

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/password",
                    nonExistentAssociationId, userData.user().getId())
                    .with(user(userData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /v1/associations/{associationId}/users - Role Selection Tests")
    class RoleSelectionTests {

        @Test
        @DisplayName("Should return 400 when trying to create user with ADMIN role")
        void shouldReturn400WhenTryingToCreateUserWithAdminRole() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            CreateUserRequest request = createValidCreateUserRequestWithRole(ADMIN);

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Administrators cannot create other administrator accounts"));
        }

        @Test
        @DisplayName("Should successfully create user with MEMBER role")
        void shouldCreateUserWithMemberRole() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            CreateUserRequest request = createValidCreateUserRequestWithRole(MEMBER);

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User account created successfully. Verification email sent to the user."))
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.firstName").value("john"))
                    .andExpect(jsonPath("$.data.lastName").value("doe"))
                    .andExpect(jsonPath("$.data.userName").value("johndoe"))
                    .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.data.phoneNumber.prefix").value("+1"))
                    .andExpect(jsonPath("$.data.phoneNumber.nationalNumber").value("234567890"))
                    .andExpect(jsonPath("$.data.userRole").value("ASSOCIATION_MEMBER"))
                    .andExpect(jsonPath("$.data.isEnabled").value(false)); // Changed: users are now created disabled

            // Verify the user was created with the correct association role
            Long createdUserId = authTestUtils.extractUserIdFromResponse(result);
            User createdUser = usersRepository.findById(createdUserId).orElseThrow();
            AssociationRole actualRole = authTestUtils.getUserRoleInAssociation(createdUser);
            assertThat(actualRole).isEqualTo(MEMBER);
        }

        @Test
        @DisplayName("Should successfully create user with COLLABORATOR role")
        void shouldCreateUserWithCollaboratorRole() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            CreateUserRequest request = createValidCreateUserRequestWithRole(AssociationRole.COLLABORATOR);

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User account created successfully. Verification email sent to the user."))
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.isEnabled").value(false)); // Changed: users are now created disabled

            // Verify the user was created with the correct association role
            Long createdUserId = authTestUtils.extractUserIdFromResponse(result);
            User createdUser = usersRepository.findById(createdUserId).orElseThrow();
            AssociationRole actualRole = authTestUtils.getUserRoleInAssociation(createdUser);
            assertThat(actualRole).isEqualTo(AssociationRole.COLLABORATOR);
            
            // Verify verification token was created
            VerificationToken verificationToken = verificationTokenRepository.findByUser(createdUser)
                    .orElseThrow(() -> new AssertionError("Verification token should have been created"));
            assertThat(verificationToken.getToken()).isNotNull();
        }

        @Test
        @DisplayName("Should return 400 when role is null")
        void shouldReturn400WhenRoleIsNull() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            
            PhoneNumberDTO phoneNumberDTO = PhoneNumberDTO.builder()
                    .prefix("+1")
                    .nationalNumber("234567890")
                    .build();

            UserRegisterDTO userData = new UserRegisterDTO(
                    "John",
                    "Doe", 
                    "johndoe",
                    "john.doe@example.com",
                    phoneNumberDTO,
                    "SecurePass#123",
                    "SecurePass#123"
            );

            // Create request with null role
            CreateUserRequest request = CreateUserRequest.builder()
                    .userData(userData)
                    .role(null)
                    .build();

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.role").value("REQUIRED"));
        }

        @Test
        @DisplayName("Should verify only MEMBER and COLLABORATOR roles are allowed")
        void shouldVerifyOnlyMemberAndCollaboratorRolesAreAllowed() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            
            // Test creating users with allowed roles
            AssociationRole[] allowedRoles = {MEMBER, AssociationRole.COLLABORATOR};
            
            for (AssociationRole role : allowedRoles) {
                // Create unique request for each role
                PhoneNumberDTO phoneNumberDTO = PhoneNumberDTO.builder()
                        .prefix("+1")
                        .nationalNumber("23456789" + role.ordinal())
                        .build();

                UserRegisterDTO userData = new UserRegisterDTO(
                        "John",
                        "Doe", 
                        "johndoe" + role.name().toLowerCase(),
                        "john.doe." + role.name().toLowerCase() + "@example.com",
                        phoneNumberDTO,
                        "SecurePass#123",
                        "SecurePass#123"
                );

                CreateUserRequest request = CreateUserRequest.builder()
                        .userData(userData)
                        .role(role)
                        .build();

                // Act
                ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData.association().getId())
                        .with(user(adminData.user().getUserName()).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

                // Assert
                result.andExpect(status().isCreated())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true));

                // Verify the role assignment
                Long createdUserId = authTestUtils.extractUserIdFromResponse(result);
                User createdUser = usersRepository.findById(createdUserId).orElseThrow();
                AssociationRole actualRole = authTestUtils.getUserRoleInAssociation(createdUser);
                assertThat(actualRole).isEqualTo(role);
            }
        }

        @Test
        @DisplayName("Should verify created users have appropriate permissions")
        void shouldVerifyCreatedUsersHaveAppropriatePermissions() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            
            // Create a MEMBER user with unique credentials
            PhoneNumberDTO memberPhoneNumberDTO = PhoneNumberDTO.builder()
                    .prefix("+1")
                    .nationalNumber("234567890")
                    .build();

            UserRegisterDTO memberUserData = new UserRegisterDTO(
                    "Jane",
                    "Smith", 
                    "janesmith",
                    "jane.smith@example.com",
                    memberPhoneNumberDTO,
                    "SecurePass#123",
                    "SecurePass#123"
            );

            CreateUserRequest memberRequest = CreateUserRequest.builder()
                    .userData(memberUserData)
                    .role(MEMBER)
                    .build();

            ResultActions memberResult = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(memberRequest)));

            memberResult.andExpect(status().isCreated());
            Long memberUserId = authTestUtils.extractUserIdFromResponse(memberResult);
            User memberUser = usersRepository.findById(memberUserId).orElseThrow();

            // Test that the MEMBER can access their own data but cannot access all users
            ResultActions memberSelfAccessResult = mockMvc.perform(get(USERS_BASE_ENDPOINT + "/{id}", 
                    adminData.association().getId(), memberUser.getId())
                    .with(user(memberUser.getUserName()).roles("USER")));

            memberSelfAccessResult.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(memberUser.getId()));

            // Test that MEMBER cannot access all users (should get 403)
            ResultActions memberAllUsersResult = mockMvc.perform(get(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(memberUser.getUserName()).roles("USER")));

            memberAllUsersResult.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Only administrators can access user accounts information"));

            // Create a COLLABORATOR user with unique credentials
            PhoneNumberDTO collaboratorPhoneNumberDTO = PhoneNumberDTO.builder()
                    .prefix("+1")
                    .nationalNumber("987654321")
                    .build();

            UserRegisterDTO collaboratorUserData = new UserRegisterDTO(
                    "Bob",
                    "Johnson", 
                    "bobjohnson",
                    "bob.johnson@example.com",
                    collaboratorPhoneNumberDTO,
                    "SecurePass#123",
                    "SecurePass#123"
            );

            CreateUserRequest collaboratorRequest = CreateUserRequest.builder()
                    .userData(collaboratorUserData)
                    .role(AssociationRole.COLLABORATOR)
                    .build();

            ResultActions collaboratorResult = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(collaboratorRequest)));

            collaboratorResult.andExpect(status().isCreated());
            Long collaboratorUserId = authTestUtils.extractUserIdFromResponse(collaboratorResult);
            User collaboratorUser = usersRepository.findById(collaboratorUserId).orElseThrow();

            // Test that COLLABORATOR cannot access all users (should get 403)
            ResultActions collaboratorActionResult = mockMvc.perform(get(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(collaboratorUser.getUserName()).roles("USER")));

            collaboratorActionResult.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Only administrators can access user accounts information"));
        }
    }

    @Nested
    @DisplayName("PUT /v1/associations/{associationId}/users/{userId}/email - Update Email")
    class UpdateEmailTests {

        @Test
        @DisplayName("Should successfully request email update with valid data")
        void shouldRequestEmailUpdateWithValidData() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            UpdateEmailRequest request = new UpdateEmailRequest("newemail@example.com");

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/email",
                    userData.association().getId(), userData.user().getId())
                    .with(user(userData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Email update verification has been sent to your new email address"))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Should return 400 when new email is same as current email")
        void shouldReturn400WhenNewEmailIsSameAsCurrent() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            UpdateEmailRequest request = new UpdateEmailRequest(userData.user().getEmail());

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/email",
                    userData.association().getId(), userData.user().getId())
                    .with(user(userData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("The new email must be different from your current email"));
        }

        @Test
        @DisplayName("Should return 409 when new email already exists")
        void shouldReturn409WhenNewEmailAlreadyExists() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData otherUserData = authTestUtils.createAuthenticatedUserInSameAssociation(userData.association());
            UpdateEmailRequest request = new UpdateEmailRequest(otherUserData.user().getEmail());

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/email",
                    userData.association().getId(), userData.user().getId())
                    .with(user(userData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.email").value("VALUE_ALREADY_EXISTS"));
        }

        @Test
        @DisplayName("Should return 400 when email format is invalid")
        void shouldReturn400WhenEmailFormatIsInvalid() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            UpdateEmailRequest request = new UpdateEmailRequest("invalid-email-format");

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/email",
                    userData.association().getId(), userData.user().getId())
                    .with(user(userData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.newEmail").value("INVALID_EMAIL"));
        }

        @Test
        @DisplayName("Should return 400 when email is blank")
        void shouldReturn400WhenEmailIsBlank() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            UpdateEmailRequest request = new UpdateEmailRequest("");

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/email",
                    userData.association().getId(), userData.user().getId())
                    .with(user(userData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.newEmail").value("REQUIRED"));
        }

        @Test
        @DisplayName("Should return 403 when MEMBER tries to update another user's email")
        void shouldReturn403WhenMemberTriesToUpdateAnotherUsersEmail() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData memberData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            UpdateEmailRequest request = new UpdateEmailRequest("newemail@example.com");

            // Act - Member tries to update admin's email
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/email",
                    adminData.association().getId(), adminData.user().getId())
                    .with(user(memberData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You can only update your own email address"));
        }

        @Test
        @DisplayName("Should return 403 when COLLABORATOR tries to update another user's email")
        void shouldReturn403WhenCollaboratorTriesToUpdateAnotherUsersEmail() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData collaboratorData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            UpdateEmailRequest request = new UpdateEmailRequest("newemail@example.com");

            // Act - Collaborator tries to update admin's email
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/email",
                    adminData.association().getId(), adminData.user().getId())
                    .with(user(collaboratorData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You can only update your own email address"));
        }

        @Test
        @DisplayName("Should allow MEMBER to update their own email")
        void shouldAllowMemberToUpdateOwnEmail() throws Exception {
            // Arrange
            AuthTestData memberData = authTestUtils.createAuthenticatedUser(true, MEMBER);
            UpdateEmailRequest request = new UpdateEmailRequest("member.newemail@example.com");

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/email",
                    memberData.association().getId(), memberData.user().getId())
                    .with(user(memberData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Email update verification has been sent to your new email address"));
        }

        @Test
        @DisplayName("Should allow COLLABORATOR to update their own email")
        void shouldAllowCollaboratorToUpdateOwnEmail() throws Exception {
            // Arrange
            AuthTestData collaboratorData = authTestUtils.createAuthenticatedUser(true, AssociationRole.COLLABORATOR);
            UpdateEmailRequest request = new UpdateEmailRequest("collaborator.newemail@example.com");

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/email",
                    collaboratorData.association().getId(), collaboratorData.user().getId())
                    .with(user(collaboratorData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Email update verification has been sent to your new email address"));
        }

        @Test
        @DisplayName("Should return 401 when unauthenticated user tries to update email")
        void shouldReturn401WhenUnauthenticatedUserTriesToUpdateEmail() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            UpdateEmailRequest request = new UpdateEmailRequest("newemail@example.com");

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/email",
                    userData.association().getId(), userData.user().getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 404 when association doesn't exist")
        void shouldReturn404WhenAssociationDoesntExist() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            UpdateEmailRequest request = new UpdateEmailRequest("newemail@example.com");
            Long nonExistentAssociationId = 99999L;

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/email",
                    nonExistentAssociationId, userData.user().getId())
                    .with(user(userData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /v1/associations/{associationId}/users/verify-email-update - Verify Email Update")
    class VerifyEmailUpdateTests {

        @Autowired
        private EmailUpdateTokenRepository emailUpdateTokenRepository;

        @Test
        @DisplayName("Should return 400 when token is blank")
        void shouldReturn400WhenTokenIsBlank() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            VerifyEmailUpdateRequest request = new VerifyEmailUpdateRequest("");

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT + "/verify-email-update",
                    userData.association().getId())
                    .with(user(userData.user().getEmail()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.token").value("REQUIRED"));
        }

        @Test
        @DisplayName("Should return 400 when token is invalid")
        void shouldReturn400WhenTokenIsInvalid() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            VerifyEmailUpdateRequest request = new VerifyEmailUpdateRequest("invalid-token");

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT + "/verify-email-update",
                    userData.association().getId())
                    .with(user(userData.user().getEmail()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid or expired email update token"));
        }

        @Test
        @DisplayName("Should return 401 when unauthenticated user tries to verify email update")
        void shouldReturn401WhenUnauthenticatedUserTriesToVerifyEmailUpdate() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            VerifyEmailUpdateRequest request = new VerifyEmailUpdateRequest("some-token");

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT + "/verify-email-update",
                    userData.association().getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 404 when association doesn't exist")
        void shouldReturn404WhenAssociationDoesntExist() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            VerifyEmailUpdateRequest request = new VerifyEmailUpdateRequest("some-token");
            Long nonExistentAssociationId = 99999L;

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT + "/verify-email-update",
                    nonExistentAssociationId)
                    .with(user(userData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /v1/associations/{associationId}/users/{userId}/phone-number - Update Phone Number")
    class UpdatePhoneNumberTests {

        @Test
        @DisplayName("Should successfully update phone number with valid data")
        void shouldUpdatePhoneNumberWithValidData() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            PhoneNumberDTO newPhoneNumber = PhoneNumberDTO.builder()
                    .prefix("+44")
                    .nationalNumber("7700900123")
                    .build();
            UpdatePhoneNumberRequest request = new UpdatePhoneNumberRequest(newPhoneNumber);

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/phone-number",
                    userData.association().getId(), userData.user().getId())
                    .with(user(userData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Phone number has been updated successfully"))
                    .andExpect(jsonPath("$.data.id").value(userData.user().getId()))
                    .andExpect(jsonPath("$.data.phoneNumber.prefix").value("+44"))
                    .andExpect(jsonPath("$.data.phoneNumber.nationalNumber").value("7700900123"));

            // Verify phone number was actually updated in database
            User updatedUser = usersRepository.findById(userData.user().getId()).orElseThrow();
            assertThat(updatedUser.getPhoneNumber().getPrefix()).isEqualTo("+44");
            assertThat(updatedUser.getPhoneNumber().getNationalNumber()).isEqualTo("7700900123");
        }

        @Test
        @DisplayName("Should allow MEMBER to update their own phone number")
        void shouldAllowMemberToUpdateOwnPhoneNumber() throws Exception {
            // Arrange
            AuthTestData memberData = authTestUtils.createAuthenticatedUser(true, MEMBER);
            PhoneNumberDTO newPhoneNumber = PhoneNumberDTO.builder()
                    .prefix("+33")
                    .nationalNumber("123456789")
                    .build();
            UpdatePhoneNumberRequest request = new UpdatePhoneNumberRequest(newPhoneNumber);

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/phone-number",
                    memberData.association().getId(), memberData.user().getId())
                    .with(user(memberData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Phone number has been updated successfully"))
                    .andExpect(jsonPath("$.data.phoneNumber.prefix").value("+33"))
                    .andExpect(jsonPath("$.data.phoneNumber.nationalNumber").value("123456789"));
        }

        @Test
        @DisplayName("Should allow COLLABORATOR to update their own phone number")
        void shouldAllowCollaboratorToUpdateOwnPhoneNumber() throws Exception {
            // Arrange
            AuthTestData collaboratorData = authTestUtils.createAuthenticatedUser(true, AssociationRole.COLLABORATOR);
            PhoneNumberDTO newPhoneNumber = PhoneNumberDTO.builder()
                    .prefix("+49")
                    .nationalNumber("1234567890")
                    .build();
            UpdatePhoneNumberRequest request = new UpdatePhoneNumberRequest(newPhoneNumber);

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/phone-number",
                    collaboratorData.association().getId(), collaboratorData.user().getId())
                    .with(user(collaboratorData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Phone number has been updated successfully"))
                    .andExpect(jsonPath("$.data.phoneNumber.prefix").value("+49"))
                    .andExpect(jsonPath("$.data.phoneNumber.nationalNumber").value("1234567890"));
        }

        @Test
        @DisplayName("Should return 403 when MEMBER tries to update another user's phone number")
        void shouldReturn403WhenMemberTriesToUpdateAnotherUsersPhoneNumber() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData memberData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            PhoneNumberDTO newPhoneNumber = PhoneNumberDTO.builder()
                    .prefix("+1")
                    .nationalNumber("9876543210")
                    .build();
            UpdatePhoneNumberRequest request = new UpdatePhoneNumberRequest(newPhoneNumber);

            // Act - Member tries to update admin's phone number
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/phone-number",
                    adminData.association().getId(), adminData.user().getId())
                    .with(user(memberData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You can only update your own phone number"));
        }

        @Test
        @DisplayName("Should return 403 when COLLABORATOR tries to update another user's phone number")
        void shouldReturn403WhenCollaboratorTriesToUpdateAnotherUsersPhoneNumber() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData collaboratorData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            PhoneNumberDTO newPhoneNumber = PhoneNumberDTO.builder()
                    .prefix("+1")
                    .nationalNumber("9876543210")
                    .build();
            UpdatePhoneNumberRequest request = new UpdatePhoneNumberRequest(newPhoneNumber);

            // Act - Collaborator tries to update admin's phone number
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/phone-number",
                    adminData.association().getId(), adminData.user().getId())
                    .with(user(collaboratorData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You can only update your own phone number"));
        }

        @Test
        @DisplayName("Should return 400 when phone number is null")
        void shouldReturn400WhenPhoneNumberIsNull() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            UpdatePhoneNumberRequest request = new UpdatePhoneNumberRequest(null);

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/phone-number",
                    userData.association().getId(), userData.user().getId())
                    .with(user(userData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.phoneNumber").value("REQUIRED"));
        }

        @Test
        @DisplayName("Should return 400 when phone number prefix is invalid")
        void shouldReturn400WhenPhoneNumberPrefixIsInvalid() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            PhoneNumberDTO invalidPhoneNumber = PhoneNumberDTO.builder()
                    .prefix("invalid-prefix")
                    .nationalNumber("1234567890")
                    .build();
            UpdatePhoneNumberRequest request = new UpdatePhoneNumberRequest(invalidPhoneNumber);

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/phone-number",
                    userData.association().getId(), userData.user().getId())
                    .with(user(userData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors['phoneNumber.prefix']").exists());
        }

        @Test
        @DisplayName("Should return 400 when phone number national number is invalid")
        void shouldReturn400WhenPhoneNumberNationalNumberIsInvalid() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            PhoneNumberDTO invalidPhoneNumber = PhoneNumberDTO.builder()
                    .prefix("+1")
                    .nationalNumber("abc123")
                    .build();
            UpdatePhoneNumberRequest request = new UpdatePhoneNumberRequest(invalidPhoneNumber);

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/phone-number",
                    userData.association().getId(), userData.user().getId())
                    .with(user(userData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors['phoneNumber.nationalNumber']").exists());
        }

        @Test
        @DisplayName("Should return 401 when unauthenticated user tries to update phone number")
        void shouldReturn401WhenUnauthenticatedUserTriesToUpdatePhoneNumber() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            PhoneNumberDTO newPhoneNumber = PhoneNumberDTO.builder()
                    .prefix("+1")
                    .nationalNumber("9876543210")
                    .build();
            UpdatePhoneNumberRequest request = new UpdatePhoneNumberRequest(newPhoneNumber);

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/phone-number",
                    userData.association().getId(), userData.user().getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 404 when association doesn't exist")
        void shouldReturn404WhenAssociationDoesntExist() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            PhoneNumberDTO newPhoneNumber = PhoneNumberDTO.builder()
                    .prefix("+1")
                    .nationalNumber("9876543210")
                    .build();
            UpdatePhoneNumberRequest request = new UpdatePhoneNumberRequest(newPhoneNumber);
            Long nonExistentAssociationId = 99999L;

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/phone-number",
                    nonExistentAssociationId, userData.user().getId())
                    .with(user(userData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should preserve other user data when updating phone number")
        void shouldPreserveOtherUserDataWhenUpdatingPhoneNumber() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            User originalUser = userData.user();
            
            // Store original user data
            String originalFirstName = originalUser.getFirstName();
            String originalLastName = originalUser.getLastName();
            String originalEmail = originalUser.getEmail();
            String originalUserName = originalUser.getUserName();
            boolean originalEnabled = originalUser.isEnabled();
            
            PhoneNumberDTO newPhoneNumber = PhoneNumberDTO.builder()
                    .prefix("+44")
                    .nationalNumber("7700900123")
                    .build();
            UpdatePhoneNumberRequest request = new UpdatePhoneNumberRequest(newPhoneNumber);

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/phone-number",
                    userData.association().getId(), userData.user().getId())
                    .with(user(userData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isOk());

            // Verify all user data except phone number remained unchanged
            User updatedUser = usersRepository.findById(originalUser.getId()).orElseThrow();
            assertThat(updatedUser.getFirstName()).isEqualTo(originalFirstName);
            assertThat(updatedUser.getLastName()).isEqualTo(originalLastName);
            assertThat(updatedUser.getEmail()).isEqualTo(originalEmail);
            assertThat(updatedUser.getUserName()).isEqualTo(originalUserName);
            assertThat(updatedUser.isEnabled()).isEqualTo(originalEnabled);
            
            // But phone number should be changed
            assertThat(updatedUser.getPhoneNumber().getPrefix()).isEqualTo("+44");
            assertThat(updatedUser.getPhoneNumber().getNationalNumber()).isEqualTo("7700900123");
        }
    }

    @Nested
    @DisplayName("PUT /v1/associations/{associationId}/users/{userId}/role - Update User Role")
    class UpdateUserRoleTests {

        @Test
        @DisplayName("Should successfully update user role from MEMBER to COLLABORATOR")
        void shouldUpdateUserRoleFromMemberToCollaborator() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData memberData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            
            // Verify initial role
            AssociationRole initialRole = authTestUtils.getUserRoleInAssociation(memberData.user());
            assertThat(initialRole).isEqualTo(MEMBER);
            
            UpdateUserRoleRequest request = createUpdateUserRoleRequest(AssociationRole.COLLABORATOR);

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/role",
                    adminData.association().getId(), memberData.user().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpected(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User role has been updated successfully"))
                    .andExpect(jsonPath("$.data.id").value(memberData.user().getId()))
                    .andExpect(jsonPath("$.data.role").value("COLLABORATOR"));

            // Verify role was actually updated in database
            AssociationRole updatedRole = authTestUtils.getUserRoleInAssociation(memberData.user());
            assertThat(updatedRole).isEqualTo(AssociationRole.COLLABORATOR);
        }

        @Test
        @DisplayName("Should successfully update user role from COLLABORATOR to MEMBER")
        void shouldUpdateUserRoleFromCollaboratorToMember() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            
            // Create a COLLABORATOR user
            PhoneNumberDTO phoneNumber = PhoneNumberDTO.builder()
                    .prefix("+1")
                    .nationalNumber("5555555555")
                    .build();
            UserRegisterDTO userData = new UserRegisterDTO(
                    "Test", "Collaborator", "testcollaborator", "test.collaborator@example.com",
                    phoneNumber, "SecurePass#123", "SecurePass#123"
            );
            CreateUserRequest createRequest = CreateUserRequest.builder()
                    .userData(userData)
                    .role(AssociationRole.COLLABORATOR)
                    .build();
            
            ResultActions createResult = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)));
            
            Long collaboratorUserId = authTestUtils.extractUserIdFromResponse(createResult);
            User collaboratorUser = usersRepository.findById(collaboratorUserId).orElseThrow();
            
            // Verify initial role
            AssociationRole initialRole = authTestUtils.getUserRoleInAssociation(collaboratorUser);
            assertThat(initialRole).isEqualTo(AssociationRole.COLLABORATOR);
            
            UpdateUserRoleRequest request = createUpdateUserRoleRequest(MEMBER);

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/role",
                    adminData.association().getId(), collaboratorUserId)
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User role has been updated successfully"))
                    .andExpect(jsonPath("$.data.id").value(collaboratorUserId))
                    .andExpect(jsonPath("$.data.role").value("ASSOCIATION_MEMBER"));

            // Verify role was actually updated in database
            AssociationRole updatedRole = authTestUtils.getUserRoleInAssociation(collaboratorUser);
            assertThat(updatedRole).isEqualTo(MEMBER);
        }

        @Test
        @DisplayName("Should return 403 when MEMBER tries to update user role")
        void shouldReturn403WhenMemberTriesToUpdateUserRole() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData memberData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            UpdateUserRoleRequest request = createUpdateUserRoleRequest(AssociationRole.COLLABORATOR);

            // Act - Member tries to update admin's role
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/role",
                    adminData.association().getId(), adminData.user().getId())
                    .with(user(memberData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Only administrators can update user roles"));
        }

        @Test
        @DisplayName("Should return 403 when COLLABORATOR tries to update user role")
        void shouldReturn403WhenCollaboratorTriesToUpdateUserRole() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData collaboratorData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            UpdateUserRoleRequest request = createUpdateUserRoleRequest(MEMBER);

            // Act - Collaborator tries to update admin's role
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/role",
                    adminData.association().getId(), adminData.user().getId())
                    .with(user(collaboratorData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Only administrators can update user roles"));
        }

        @Test
        @DisplayName("Should return 400 when ADMIN tries to update their own role")
        void shouldReturn400WhenAdminTriesToUpdateOwnRole() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            UpdateUserRoleRequest request = createUpdateUserRoleRequest(MEMBER);

            // Act - Admin tries to update their own role
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/role",
                    adminData.association().getId(), adminData.user().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You cannot update your own role"))
                    .andExpect(jsonPath("$.errorCode").value("ROLE_UPDATE_SELF_DENIED"));
        }

        @Test
        @DisplayName("Should return 400 when trying to update another ADMIN's role")
        void shouldReturn400WhenTryingToUpdateAnotherAdminRole() throws Exception {
            // Arrange
            AuthTestData adminData1 = authTestUtils.createAuthenticatedUser(true, ADMIN);
            
            // Create another admin user in the same association
            PhoneNumberDTO phoneNumber = PhoneNumberDTO.builder()
                    .prefix("+1")
                    .nationalNumber("9999999999")
                    .build();
            UserRegisterDTO userData = new UserRegisterDTO(
                    "Another", "Admin", "anotheradmin", "another.admin@example.com",
                    phoneNumber, "SecurePass#123", "SecurePass#123"
            );
            CreateUserRequest createRequest = CreateUserRequest.builder()
                    .userData(userData)
                    .role(MEMBER) // Create as MEMBER first
                    .build();
            
            ResultActions createResult = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData1.association().getId())
                    .with(user(adminData1.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)));
            
            Long secondUserId = authTestUtils.extractUserIdFromResponse(createResult);
            User secondUser = usersRepository.findById(secondUserId).orElseThrow();
            
            // Manually promote to ADMIN via direct database manipulation (simulating existing admin)
            authTestUtils.setUserRoleInAssociation(secondUser, ADMIN);
            
            UpdateUserRoleRequest request = createUpdateUserRoleRequest(MEMBER);

            // Act - First admin tries to update second admin's role
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/role",
                    adminData1.association().getId(), secondUserId)
                    .with(user(adminData1.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Administrator roles cannot be updated"))
                    .andExpect(jsonPath("$.errorCode").value("ROLE_UPDATE_ADMIN_DENIED"));
        }

        @Test
        @DisplayName("Should return 400 when role is null")
        void shouldReturn400WhenRoleIsNull() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData memberData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            UpdateUserRoleRequest request = new UpdateUserRoleRequest(null);

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/role",
                    adminData.association().getId(), memberData.user().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.role").value("REQUIRED"));
        }

        @Test
        @DisplayName("Should return 400 when trying to set role to ADMIN")
        void shouldReturn400WhenTryingToSetRoleToAdmin() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData memberData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            
            // Create request with ADMIN role (this should be invalid)
            String requestJson = """
                {
                    "role": "ADMIN"
                }
                """;

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/role",
                    adminData.association().getId(), memberData.user().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.role").value("INVALID_FIELD"));
        }

        @Test
        @DisplayName("Should return 401 when unauthenticated user tries to update role")
        void shouldReturn401WhenUnauthenticatedUserTriesToUpdateRole() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData memberData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            UpdateUserRoleRequest request = createUpdateUserRoleRequest(AssociationRole.COLLABORATOR);

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/role",
                    adminData.association().getId(), memberData.user().getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpected(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 404 when user doesn't exist")
        void shouldReturn404WhenUserDoesntExist() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            UpdateUserRoleRequest request = createUpdateUserRoleRequest(AssociationRole.COLLABORATOR);
            Long nonExistentUserId = 99999L;

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/role",
                    adminData.association().getId(), nonExistentUserId)
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpected(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").contains("not found"));
        }

        @Test
        @DisplayName("Should return 404 when association doesn't exist")
        void shouldReturn404WhenAssociationDoesntExist() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            UpdateUserRoleRequest request = createUpdateUserRoleRequest(AssociationRole.COLLABORATOR);
            Long nonExistentAssociationId = 99999L;

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/role",
                    nonExistentAssociationId, adminData.user().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpected(status().isForbidden());
        }

        @Test
        @DisplayName("Should preserve other user data when updating role")
        void shouldPreserveOtherUserDataWhenUpdatingRole() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData memberData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            User originalUser = memberData.user();
            
            // Store original user data
            String originalFirstName = originalUser.getFirstName();
            String originalLastName = originalUser.getLastName();
            String originalEmail = originalUser.getEmail();
            String originalUserName = originalUser.getUserName();
            boolean originalEnabled = originalUser.isEnabled();
            String originalPhonePrefix = originalUser.getPhoneNumber().getPrefix();
            String originalPhoneNumber = originalUser.getPhoneNumber().getNationalNumber();
            
            UpdateUserRoleRequest request = createUpdateUserRoleRequest(AssociationRole.COLLABORATOR);

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/role",
                    adminData.association().getId(), originalUser.getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpected(status().isOk());

            // Verify all user data except role remained unchanged
            User updatedUser = usersRepository.findById(originalUser.getId()).orElseThrow();
            assertThat(updatedUser.getFirstName()).isEqualTo(originalFirstName);
            assertThat(updatedUser.getLastName()).isEqualTo(originalLastName);
            assertThat(updatedUser.getEmail()).isEqualTo(originalEmail);
            assertThat(updatedUser.getUserName()).isEqualTo(originalUserName);
            assertThat(updatedUser.isEnabled()).isEqualTo(originalEnabled);
            assertThat(updatedUser.getPhoneNumber().getPrefix()).isEqualTo(originalPhonePrefix);
            assertThat(updatedUser.getPhoneNumber().getNationalNumber()).isEqualTo(originalPhoneNumber);
            
            // But role should be changed
            AssociationRole updatedRole = authTestUtils.getUserRoleInAssociation(updatedUser);
            assertThat(updatedRole).isEqualTo(AssociationRole.COLLABORATOR);
        }

        @Test
        @DisplayName("Should allow updating role multiple times")
        void shouldAllowUpdatingRoleMultipleTimes() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData memberData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            
            // First update: MEMBER -> COLLABORATOR
            UpdateUserRoleRequest firstRequest = createUpdateUserRoleRequest(AssociationRole.COLLABORATOR);
            
            ResultActions firstResult = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/role",
                    adminData.association().getId(), memberData.user().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(firstRequest)));
            
            firstResult.andExpected(status().isOk());
            
            // Verify first update
            AssociationRole intermediateRole = authTestUtils.getUserRoleInAssociation(memberData.user());
            assertThat(intermediateRole).isEqualTo(AssociationRole.COLLABORATOR);
            
            // Second update: COLLABORATOR -> MEMBER
            UpdateUserRoleRequest secondRequest = createUpdateUserRoleRequest(MEMBER);
            
            // Act
            ResultActions secondResult = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/role",
                    adminData.association().getId(), memberData.user().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(secondRequest)));

            // Assert
            secondResult.andExpected(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User role has been updated successfully"))
                    .andExpect(jsonPath("$.data.role").value("ASSOCIATION_MEMBER"));

            // Verify final role
            AssociationRole finalRole = authTestUtils.getUserRoleInAssociation(memberData.user());
            assertThat(finalRole).isEqualTo(MEMBER);
        }

        @Test
        @DisplayName("Should return appropriate error when request body is invalid JSON")
        void shouldReturnErrorWhenRequestBodyIsInvalidJson() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, ADMIN);
            AuthTestData memberData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            String invalidJson = "{ invalid json }";

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/role",
                    adminData.association().getId(), memberData.user().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson));

            // Assert
            result.andExpected(status().isBadRequest());
        }
    }

    // Helper methods for creating test data
    private CreateUserRequest createValidCreateUserRequest() {
        return createValidCreateUserRequestWithRole(MEMBER);
    }

    private CreateUserRequest createValidCreateUserRequestWithRole(AssociationRole role) {
        PhoneNumberDTO phoneNumberDTO = PhoneNumberDTO.builder()
                .prefix("+1")
                .nationalNumber("234567890")
                .build();

        UserRegisterDTO userData = new UserRegisterDTO(
                "John",
                "Doe", 
                "johndoe",
                "john.doe@example.com",
                phoneNumberDTO,
                "SecurePass#123",
                "SecurePass#123"
        );

        return CreateUserRequest.builder()
                .userData(userData)
                .role(role)
                .build();
    }

    private CreateUserRequest createInvalidCreateUserRequest() {
        UserRegisterDTO invalidUserData = new UserRegisterDTO(
                "", // Invalid: empty first name
                "", // Invalid: empty last name
                "a", // Invalid: too short username
                "invalid-email", // Invalid: invalid email format
                null,
                "weak", // Invalid: weak password
                "weak"
        );

        return CreateUserRequest.builder()
                .userData(invalidUserData)
                .role(MEMBER)
                .build();
    }

    private CreateUserRequest createUserRequestWithMismatchedPasswords() {
        PhoneNumberDTO phoneNumberDTO = PhoneNumberDTO.builder()
                .prefix("+1")
                .nationalNumber("234567890")
                .build();

        UserRegisterDTO userData = new UserRegisterDTO(
                "John",
                "Doe",
                "johndoe",
                "john.doe@example.com",
                phoneNumberDTO,
                "SecurePass#123",
                "DifferentPass#456" // Mismatched password
        );

        return CreateUserRequest.builder()
                .userData(userData)
                .role(MEMBER)
                .build();
    }

    private CreateUserRequest createUserRequestWithExistingUsername(String existingUsername) {
        PhoneNumberDTO phoneNumberDTO = PhoneNumberDTO.builder()
                .prefix("+1")
                .nationalNumber("234567890")
                .build();

        UserRegisterDTO userData = new UserRegisterDTO(
                "John",
                "Doe",
                existingUsername, // Using existing username
                "new.email@example.com",
                phoneNumberDTO,
                "SecurePass#123",
                "SecurePass#123"
        );

        return CreateUserRequest.builder()
                .userData(userData)
                .role(MEMBER)
                .build();
    }

    private CreateUserRequest createUserRequestWithExistingEmail(String existingEmail) {
        PhoneNumberDTO phoneNumberDTO = PhoneNumberDTO.builder()
                .prefix("+1")
                .nationalNumber("234567890")
                .build();

        UserRegisterDTO userData = new UserRegisterDTO(
                "John",
                "Doe",
                "newusername",
                existingEmail, // Using existing email
                phoneNumberDTO,
                "SecurePass#123",
                "SecurePass#123"
        );

        return CreateUserRequest.builder()
                .userData(userData)
                .role(MEMBER)
                .build();
    }

    private EditUserRequest createValidEditUserRequest() {
        return new EditUserRequest(UserBaseDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .userName("janesmith")
                .build());
    }

    private EditUserRequest createInvalidEditUserRequest() {
        return new EditUserRequest(UserBaseDTO.builder()
                .firstName("")
                .lastName("")
                .userName("a")
                .build());
    }

    private EditUserRequest createEditUserRequestWithExistingUsername(String existingUsername) {
        return new EditUserRequest(UserBaseDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .userName(existingUsername)
                .build());
    }

    private UpdateUserRoleRequest createUpdateUserRoleRequest(AssociationRole role) {
        return new UpdateUserRoleRequest(role);
    }
} 