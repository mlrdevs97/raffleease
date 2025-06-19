package com.raffleease.raffleease.Domains.Users.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffleease.raffleease.Base.AbstractIntegrationTest;
import com.raffleease.raffleease.Common.Models.BaseUserData;
import com.raffleease.raffleease.Common.Models.CreateUserData;
import com.raffleease.raffleease.Common.Models.PhoneNumber;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationRole;
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
        @DisplayName("Should return array with only authenticated user when association has no other users")
        void shouldReturnArrayWithOnlyAuthenticatedUser() throws Exception {
            // Arrange - Create a single authenticated user
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);

            // Act - Query the association (should return the authenticated user only)
            ResultActions result = mockMvc.perform(get(USERS_BASE_ENDPOINT, adminData.association().getId())
                    .with(user(adminData.user().getUserName()).roles("USER")));

            // Assert - Should return array with exactly one user (the authenticated user)
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].id").value(adminData.user().getId()))
                    .andExpect(jsonPath("$.data[0].userName").value(adminData.user().getUserName()));
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
        @DisplayName("Should successfully return user by ID")
        void shouldReturnUserById() throws Exception {
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
        @DisplayName("Should return 404 when user doesn't exist")
        void shouldReturn404WhenUserDoesntExist() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
            Long nonExistentUserId = 99999L;

            // Act
            ResultActions result = mockMvc.perform(get(USERS_BASE_ENDPOINT + "/{id}", 
                    adminData.association().getId(), nonExistentUserId)
                    .with(user(adminData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false));
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
        @DisplayName("Should successfully update user with valid data")
        void shouldUpdateUserWithValidData() throws Exception {
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
        @DisplayName("Should return 400 when request data is invalid")
        void shouldReturn400WhenRequestDataIsInvalid() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
            User targetUser = adminData.user();
            EditUserRequest invalidRequest = createInvalidEditUserRequest();

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{id}", 
                    adminData.association().getId(), targetUser.getId())
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 404 when user doesn't exist")
        void shouldReturn404WhenUserDoesntExist() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
            Long nonExistentUserId = 99999L;
            EditUserRequest request = createValidEditUserRequest();

            // Act
            ResultActions result = mockMvc.perform(put(USERS_BASE_ENDPOINT + "/{id}", 
                    adminData.association().getId(), nonExistentUserId)
                    .with(user(adminData.user().getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result.andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 409 when updated username already exists")
        void shouldReturn409WhenUpdatedUsernameAlreadyExists() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
            AuthTestData otherUserData = authTestUtils.createAuthenticatedUserInSameAssociation(adminData.association());
            
            User adminUser = usersRepository.findById(adminData.user().getId()).orElseThrow();
            User otherUser = usersRepository.findById(otherUserData.user().getId()).orElseThrow();
            
            // Ensure the users have different usernames
            assertThat(adminUser.getUserName()).isNotEqualTo(otherUser.getUserName());
            assertThat(adminUser.getId()).isNotEqualTo(otherUser.getId());
            
            EditUserRequest request = createEditUserRequestWithExistingUsername(otherUser.getUserName());

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
        @DisplayName("Should successfully disable user")
        void shouldDisableUser() throws Exception {
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
        @DisplayName("Should return 404 when user doesn't exist")
        void shouldReturn404WhenUserDoesntExist() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
            Long nonExistentUserId = 99999L;

            // Act
            ResultActions result = mockMvc.perform(patch(USERS_BASE_ENDPOINT + "/{userId}/disable", 
                    adminData.association().getId(), nonExistentUserId)
                    .with(user(adminData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isNotFound());
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
        @DisplayName("Should successfully enable user")
        void shouldEnableUser() throws Exception {
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
        @DisplayName("Should return 404 when user doesn't exist")
        void shouldReturn404WhenUserDoesntExist() throws Exception {
            // Arrange
            AuthTestData adminData = authTestUtils.createAuthenticatedUser(true, AssociationRole.ADMIN);
            Long nonExistentUserId = 99999L;

            // Act
            ResultActions result = mockMvc.perform(patch(USERS_BASE_ENDPOINT + "/{userId}/enable", 
                    adminData.association().getId(), nonExistentUserId)
                    .with(user(adminData.user().getUserName()).roles("USER")));

            // Assert
            result.andExpect(status().isNotFound());
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

    // Helper methods for creating test data
    private CreateUserRequest createValidCreateUserRequest() {
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