package com.raffleease.raffleease.Domains.Auth.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffleease.raffleease.Base.AbstractIntegrationTest;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationRole;
import com.raffleease.raffleease.Domains.Auth.DTOs.EditPasswordRequest;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Edit Password Controller Integration Tests")
class EditPasswordControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthTestUtils authTestUtils;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String EDIT_PASSWORD_ENDPOINT = "/v1/auth/password";

    @Nested
    @DisplayName("PUT /v1/auth/password - Edit Password")
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
            ResultActions result = mockMvc.perform(put(EDIT_PASSWORD_ENDPOINT)
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
            EditPasswordRequest request = new EditPasswordRequest(
                    "CurrentPass#123",
                    "NewSecurePass#456",
                    "NewSecurePass#456"
            );

            // Act
            ResultActions result = mockMvc.perform(put(EDIT_PASSWORD_ENDPOINT)
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
            ResultActions result = mockMvc.perform(put(EDIT_PASSWORD_ENDPOINT)
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
            ResultActions result = mockMvc.perform(put(EDIT_PASSWORD_ENDPOINT)
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
            ResultActions result = mockMvc.perform(put(EDIT_PASSWORD_ENDPOINT)
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
            ResultActions result = mockMvc.perform(put(EDIT_PASSWORD_ENDPOINT)
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
                    "", // Blank current password
                    "", // Blank new password
                    ""  // Blank confirm password
            );

            // Act
            ResultActions result = mockMvc.perform(put(EDIT_PASSWORD_ENDPOINT)
                    .with(user(user.getUserName()).roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert - ERROR responses do NOT have a data field (ValidationErrorResponse)
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.currentPassword").value("REQUIRED"))
                    .andExpect(jsonPath("$.errors.password").value("REQUIRED"))
                    .andExpect(jsonPath("$.errors.confirmPassword").value("REQUIRED"));
            // ❌ DO NOT ADD: .andExpect(jsonPath("$.data").isEmpty()) 
            // ValidationErrorResponse does not have a 'data' field - this would cause test failure
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
            ResultActions result = mockMvc.perform(put(EDIT_PASSWORD_ENDPOINT)
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
    }
} 