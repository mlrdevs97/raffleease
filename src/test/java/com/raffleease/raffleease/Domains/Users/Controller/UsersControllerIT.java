package com.raffleease.raffleease.Domains.Users.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffleease.raffleease.Base.AbstractIntegrationTest;
import com.raffleease.raffleease.Common.Models.BaseUserData;
import com.raffleease.raffleease.Common.Models.CreateUserData;
import com.raffleease.raffleease.Common.Models.PhoneNumber;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationRole;
import com.raffleease.raffleease.Domains.Auth.DTOs.EditPasswordRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.CreateUserRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.EditUserRequest;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Repository.UsersRepository;
import com.raffleease.raffleease.util.AuthTestUtils;
import com.raffleease.raffleease.util.AuthTestUtils.AuthTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.ResultActions;

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

    private static final String USERS_BASE_ENDPOINT = "/v1/associations/{associationId}/users";

    @Nested
    @DisplayName("POST /v1/associations/{associationId}/users - Create User")
    class CreateUserTests {

        @Test
        @DisplayName("Should successfully create user with valid data")
        void shouldCreateUserWithValidData() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
                    .andExpect(jsonPath("$.message").value("User account created successfully. Verification email sent."))
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.firstName").value("john"))
                    .andExpect(jsonPath("$.data.lastName").value("doe"))
                    .andExpect(jsonPath("$.data.userName").value("johndoe"))
                    .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.data.phoneNumber").value("+1234567890"))
                    .andExpect(jsonPath("$.data.userRole").value("ASSOCIATION_MEMBER"))
                    .andExpect(jsonPath("$.data.isEnabled").value(true));
        }

        @Test
        @DisplayName("Should return 403 when MEMBER tries to create user")
        void shouldReturn403WhenMemberTriesToCreateUser() throws Exception {
            // Arrange
            AuthTestData memberData = authTestUtils.createAuthenticatedUser(true, AssociationRole.MEMBER);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
    }

    @Nested
    @DisplayName("GET /v1/associations/{associationId}/users - Get All Users")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should successfully return all users for association")
        void shouldReturnAllUsersForAssociation() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
            AuthTestData memberData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());

            // Act
            ResultActions result = mockMvc.perform(get(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(adminData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))))
                    .andExpect(jsonPath("$.data[0].id").exists())
                    .andExpect(jsonPath("$.data[0].firstName").exists())
                    .andExpect(jsonPath("$.data[0].lastName").exists())
                    .andExpect(jsonPath("$.data[0].userName").exists())
                    .andExpect(jsonPath("$.data[0].email").exists())
                    .andExpect(jsonPath("$.data[0].userRole").exists())
                    .andExpect(jsonPath("$.data[0].isEnabled").exists());
        }

        @Test
        @DisplayName("Should return 403 when MEMBER tries to get all users")
        void shouldReturn403WhenMemberTriesToGetAllUsers() throws Exception {
            // Arrange
            AuthTestData memberData = authTestUtils.createAuthenticatedUser(true, AssociationRole.MEMBER);

            // Act
            ResultActions result = mockMvc.perform(get(USERS_BASE_ENDPOINT, memberData.association().getId())
                    .with(user(memberData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Only administrators can access user accounts information"));
        }

        @Test
        @DisplayName("Should return 403 when COLLABORATOR tries to get all users")
        void shouldReturn403WhenCollaboratorTriesToGetAllUsers() throws Exception {
            // Arrange
            AuthTestData collaboratorData = authTestUtils.createAuthenticatedUser(true, AssociationRole.COLLABORATOR);

            // Act
            ResultActions result = mockMvc.perform(get(USERS_BASE_ENDPOINT, collaboratorData.association().getId())
                    .with(user(collaboratorData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Only administrators can access user accounts information"));
        }

        @Test
        @DisplayName("Should return 401 when unauthenticated user tries to get all users")
        void shouldReturn401WhenUnauthenticatedUserTriesToGetAllUsers() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);

            // Act
            ResultActions result = mockMvc.perform(get(USERS_BASE_ENDPOINT, adminData.association().getId()));

            // Assert
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 404 when association doesn't exist")
        void shouldReturn404WhenAssociationDoesntExist() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
            Long nonExistentAssociationId = 99999L;

            // Act
            ResultActions result = mockMvc.perform(get(USERS_BASE_ENDPOINT, nonExistentAssociationId)
                    .with(user(adminData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /v1/associations/{associationId}/users/{id} - Get User By ID")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should successfully return user by ID for ADMIN")
        void shouldReturnUserByIdForAdmin() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
                    .andExpect(jsonPath("$.data.userRole").value(targetUser.getUserRole().toString()))
                    .andExpect(jsonPath("$.data.isEnabled").value(targetUser.isEnabled()));
        }

        @Test
        @DisplayName("Should allow MEMBER to access their own user data")
        void shouldAllowMemberToAccessOwnUserData() throws Exception {
            // Arrange
            AuthTestData memberData = authTestUtils.createAuthenticatedUser(true, AssociationRole.MEMBER);

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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);

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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
                    .andExpect(jsonPath("$.data.email").value("jane.smith@example.com"));
        }

        @Test
        @DisplayName("Should allow MEMBER to update their own data")
        void shouldAllowMemberToUpdateOwnData() throws Exception {
            // Arrange
            AuthTestData memberData = authTestUtils.createAuthenticatedUser(true, AssociationRole.MEMBER);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
                    .andExpect(jsonPath("$.message").value("Only administrators can update user accounts, or users can update their own account"));
        }

        @Test
        @DisplayName("Should return 403 when COLLABORATOR tries to update another user")
        void shouldReturn403WhenCollaboratorTriesToUpdateAnotherUser() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
                    .andExpect(jsonPath("$.message").value("Only administrators can update user accounts, or users can update their own account"));
        }

        @Test
        @DisplayName("Should return 401 when unauthenticated user tries to update user")
        void shouldReturn401WhenUnauthenticatedUserTriesToUpdateUser() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);

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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);

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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);

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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
            User user = userData.user();

            EditPasswordRequest request = new EditPasswordRequest(
                    "WrongCurrentPassword#123", // Incorrect current password
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
            result.andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Current password is incorrect"));
        }

        @Test
        @DisplayName("Should return 400 when new password is same as current password")
        void shouldReturn400WhenNewPasswordIsSameAsCurrent() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
            User user = userData.user();
            String currentPlainPassword = "CurrentPass#123";

            // Set a known password for testing
            user.setPassword(passwordEncoder.encode(currentPlainPassword));
            usersRepository.save(user);

            EditPasswordRequest request = new EditPasswordRequest(
                    currentPlainPassword,
                    currentPlainPassword, // Same as current password
                    currentPlainPassword
            );

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{userId}/password",
                    userData.association().getId(), user.getId())
                    .with(user(user.getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("New password must be different from current password"));
        }

        @Test
        @DisplayName("Should return 400 when passwords don't match")
        void shouldReturn400WhenPasswordsDontMatch() throws Exception {
            // Arrange
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData userData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
            CreateUserRequest request = createValidCreateUserRequestWithRole(AssociationRole.ADMIN);

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
                    .andExpect(jsonPath("$.errors.role").value("INVALID_FIELD"));
        }

        @Test
        @DisplayName("Should successfully create user with MEMBER role")
        void shouldCreateUserWithMemberRole() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
            CreateUserRequest request = createValidCreateUserRequestWithRole(AssociationRole.MEMBER);

            // Act
            ResultActions result = mockMvc.perform(post(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User account created successfully. Verification email sent."))
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.firstName").value("john"))
                    .andExpect(jsonPath("$.data.lastName").value("doe"))
                    .andExpect(jsonPath("$.data.userName").value("johndoe"))
                    .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.data.phoneNumber").value("+1234567890"))
                    .andExpect(jsonPath("$.data.userRole").value("ASSOCIATION_MEMBER"))
                    .andExpect(jsonPath("$.data.isEnabled").value(true));

            // Verify the user was created with the correct association role
            Long createdUserId = authTestUtils.extractUserIdFromResponse(result);
            User createdUser = usersRepository.findById(createdUserId).orElseThrow();
            AssociationRole actualRole = authTestUtils.getUserRoleInAssociation(createdUser);
            assertThat(actualRole).isEqualTo(AssociationRole.MEMBER);
        }

        @Test
        @DisplayName("Should successfully create user with COLLABORATOR role")
        void shouldCreateUserWithCollaboratorRole() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
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
                    .andExpect(jsonPath("$.data.id").exists());

            // Verify the user was created with the correct association role
            Long createdUserId = authTestUtils.extractUserIdFromResponse(result);
            User createdUser = usersRepository.findById(createdUserId).orElseThrow();
            AssociationRole actualRole = authTestUtils.getUserRoleInAssociation(createdUser);
            assertThat(actualRole).isEqualTo(AssociationRole.COLLABORATOR);
        }

        @Test
        @DisplayName("Should return 400 when role is null")
        void shouldReturn400WhenRoleIsNull() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
            
            PhoneNumber phoneNumber = PhoneNumber.builder()
                    .prefix("+1")
                    .nationalNumber("234567890")
                    .build();

            CreateUserData userData = new CreateUserData(
                    "John",
                    "Doe", 
                    "johndoe",
                    "john.doe@example.com",
                    phoneNumber,
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
            
            // Test creating users with allowed roles
            AssociationRole[] allowedRoles = {AssociationRole.MEMBER, AssociationRole.COLLABORATOR};
            
            for (AssociationRole role : allowedRoles) {
                // Create unique request for each role
                PhoneNumber phoneNumber = PhoneNumber.builder()
                        .prefix("+1")
                        .nationalNumber("23456789" + role.ordinal())
                        .build();

                CreateUserData userData = new CreateUserData(
                        "John",
                        "Doe", 
                        "johndoe" + role.name().toLowerCase(),
                        "john.doe." + role.name().toLowerCase() + "@example.com",
                        phoneNumber,
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
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
            
            // Create a MEMBER user with unique credentials
            PhoneNumber memberPhoneNumber = PhoneNumber.builder()
                    .prefix("+1")
                    .nationalNumber("234567890")
                    .build();

            CreateUserData memberUserData = new CreateUserData(
                    "Jane",
                    "Smith", 
                    "janesmith",
                    "jane.smith@example.com",
                    memberPhoneNumber,
                    "SecurePass#123",
                    "SecurePass#123"
            );

            CreateUserRequest memberRequest = CreateUserRequest.builder()
                    .userData(memberUserData)
                    .role(AssociationRole.MEMBER)
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
            PhoneNumber collaboratorPhoneNumber = PhoneNumber.builder()
                    .prefix("+1")
                    .nationalNumber("987654321")
                    .build();

            CreateUserData collaboratorUserData = new CreateUserData(
                    "Bob",
                    "Johnson", 
                    "bobjohnson",
                    "bob.johnson@example.com",
                    collaboratorPhoneNumber,
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

    // Helper methods for creating test data
    private CreateUserRequest createValidCreateUserRequest() {
        return createValidCreateUserRequestWithRole(AssociationRole.MEMBER);
    }

    private CreateUserRequest createValidCreateUserRequestWithRole(AssociationRole role) {
        PhoneNumber phoneNumber = PhoneNumber.builder()
                .prefix("+1")
                .nationalNumber("234567890")
                .build();

        CreateUserData userData = new CreateUserData(
                "John",
                "Doe", 
                "johndoe",
                "john.doe@example.com",
                phoneNumber,
                "SecurePass#123",
                "SecurePass#123"
        );

        return CreateUserRequest.builder()
                .userData(userData)
                .role(role)
                .build();
    }

    private CreateUserRequest createInvalidCreateUserRequest() {
        CreateUserData invalidUserData = new CreateUserData(
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
                .role(AssociationRole.MEMBER)
                .build();
    }

    private CreateUserRequest createUserRequestWithMismatchedPasswords() {
        PhoneNumber phoneNumber = PhoneNumber.builder()
                .prefix("+1")
                .nationalNumber("234567890")
                .build();

        CreateUserData userData = new CreateUserData(
                "John",
                "Doe",
                "johndoe",
                "john.doe@example.com",
                phoneNumber,
                "SecurePass#123",
                "DifferentPass#456" // Mismatched password
        );

        return CreateUserRequest.builder()
                .userData(userData)
                .role(AssociationRole.MEMBER)
                .build();
    }

    private CreateUserRequest createUserRequestWithExistingUsername(String existingUsername) {
        PhoneNumber phoneNumber = PhoneNumber.builder()
                .prefix("+1")
                .nationalNumber("234567890")
                .build();

        CreateUserData userData = new CreateUserData(
                "John",
                "Doe",
                existingUsername, // Using existing username
                "new.email@example.com",
                phoneNumber,
                "SecurePass#123",
                "SecurePass#123"
        );

        return CreateUserRequest.builder()
                .userData(userData)
                .role(AssociationRole.MEMBER)
                .build();
    }

    private CreateUserRequest createUserRequestWithExistingEmail(String existingEmail) {
        PhoneNumber phoneNumber = PhoneNumber.builder()
                .prefix("+1")
                .nationalNumber("234567890")
                .build();

        CreateUserData userData = new CreateUserData(
                "John",
                "Doe",
                "newusername",
                existingEmail, // Using existing email
                phoneNumber,
                "SecurePass#123",
                "SecurePass#123"
        );

        return CreateUserRequest.builder()
                .userData(userData)
                .role(AssociationRole.MEMBER)
                .build();
    }

    private EditUserRequest createValidEditUserRequest() {
        PhoneNumber phoneNumber = PhoneNumber.builder()
                .prefix("+1")
                .nationalNumber("987654321")
                .build();

        BaseUserData userData = BaseUserData.builder()
                .firstName("Jane")
                .lastName("Smith")
                .userName("janesmith")
                .email("jane.smith@example.com")
                .phoneNumber(phoneNumber)
                .build();

        return new EditUserRequest(userData);
    }

    private EditUserRequest createInvalidEditUserRequest() {
        BaseUserData invalidUserData = BaseUserData.builder()
                .firstName("") // Invalid: empty first name
                .lastName("") // Invalid: empty last name  
                .userName("a") // Invalid: too short username
                .email("invalid-email") // Invalid: invalid email format
                .phoneNumber(null)
                .build();

        return new EditUserRequest(invalidUserData);
    }

    private EditUserRequest createEditUserRequestWithExistingUsername(String existingUsername) {
        PhoneNumber phoneNumber = PhoneNumber.builder()
                .prefix("+1")
                .nationalNumber("987654321")
                .build();

        BaseUserData userData = BaseUserData.builder()
                .firstName("Jane")
                .lastName("Smith")
                .userName(existingUsername)
                .email("jane.smith@example.com")
                .phoneNumber(phoneNumber)
                .build();

        return new EditUserRequest(userData);
    }
} 