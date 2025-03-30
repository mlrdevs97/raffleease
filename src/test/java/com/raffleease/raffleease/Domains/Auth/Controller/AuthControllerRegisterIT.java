package com.raffleease.raffleease.Domains.Auth.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffleease.raffleease.Domains.Associations.Repository.AssociationsRepository;
import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;
import com.raffleease.raffleease.Domains.Users.Repository.CustomUsersRepository;
import com.raffleease.raffleease.Domains.Users.Repository.UsersRepository;
import com.raffleease.raffleease.Helpers.AssociationRegisterBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(SpringExtension.class)
class AuthControllerRegisterIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomUsersRepository customUsersRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AssociationsRepository associationsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private AssociationRegisterBuilder validBuilder;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");

    @BeforeEach
    void setUp() {
        validBuilder = new AssociationRegisterBuilder();
    }

    @AfterEach
    void cleanDatabase() {
        associationsRepository.deleteAll();
        usersRepository.deleteAll();
    }

    @Test
    void ConnectionEstablished() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void shouldRegisterSuccessfullyWithValidData() throws Exception {
        AssociationRegister request = validBuilder.build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Association registered successfully"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refresh_token"));

        assertThat(customUsersRepository.findByIdentifier(request.email())).isPresent();
    }

    @Test
    void shouldFailIfPasswordIsMissing() throws Exception {
        AssociationRegister request = validBuilder.withPassword(null).build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password is required"));
    }

    @Test
    void shouldFailIfPasswordDoesNotMeetRegex() throws Exception {
        AssociationRegister request = validBuilder.withPassword("weakpass").withConfirmPassword("weakpass").build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value(containsString("Password must be between 8 and 32 characters")));
    }

    @Test
    void shouldFailIfConfirmPasswordIsMissing() throws Exception {
        AssociationRegister request = validBuilder.withConfirmPassword(null).build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.confirmPassword").value("Confirm password is required"));
    }

    @Test
    void shouldFailIfPasswordDoesNotMatchConfirmPassword() throws Exception {
        AssociationRegister request = validBuilder.withPassword("MySecurePassword#123").withConfirmPassword("SecurePassword#123").build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.confirmPassword").value("Password and confirm password don't match"));
    }

    @Test
    void shouldFailIfNameDoesNotMatchSizeConstraint() throws Exception {
        AssociationRegister request = validBuilder.withName("A").build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Name must be between 2 and 100 characters"));
    }

    @Test
    void shouldFailIfNameIsMissing() throws Exception {
        AssociationRegister request = validBuilder.withName(null).build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Association's name is required"));
    }

    @Test
    void shouldFailIfEmailIsInvalid() throws Exception {
        AssociationRegister request = validBuilder.withEmail("invalidemail").build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Must provide a valid email"));
    }

    @Test
    void shouldFailIfEMailIsMissing() throws Exception {
        AssociationRegister request = validBuilder.withEmail(null).build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Association's email is required"));
    }

    @Test
    void shouldFailIfPhoneNumberIsInvalid() throws Exception {
        AssociationRegister request = validBuilder.withPhoneNumber("invalid").build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.phoneNumber").value("Must provide a valid phone number"));
    }

    @Test
    void shouldFailIfPhoneNumberIsMissing() throws Exception {
        AssociationRegister request = validBuilder.withName(null).build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Association's name is required"));
    }

    @Test
    void shouldFailIfCityIsDoesNotMatchSizeConstraint() throws Exception {
        AssociationRegister request = validBuilder.withCity("A").build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.city").value("City must be between 2 and 100 characters"));
    }

    @Test
    void shouldFailIfCityIsMissing() throws Exception {
        AssociationRegister request = validBuilder.withCity(null).build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.city").value("Association's city is required"));
    }

    @Test
    void shouldFailIfProvinceDoesNotMatchSizeConstraint() throws Exception {
        AssociationRegister request = validBuilder.withProvince("A").build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.province").value("Province must be between 2 and 100 characters"));
    }

    @Test
    void shouldFailIfProvinceIsMissing() throws Exception {
        AssociationRegister request = validBuilder.withProvince(null).build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.province").value("Association's province is required"));
    }

    @Test
    void shouldFailIfZipCodeIsMissing() throws Exception {
        AssociationRegister request = validBuilder.withZipCode(null).build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.zipCode").value("Association's zip code is required"));
    }

    @Test
    void shouldFailIfZipCodeIsInvalid() throws Exception {
        AssociationRegister request = validBuilder.withZipCode("123").build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.zipCode").value("Must provide a valid zip code"));
    }

    @Test
    void shouldFailIfEmailIsAlreadyUsed() throws Exception {
        AssociationRegister request = validBuilder.build();
        performSuccessfulRegister(request);

        AssociationRegister duplicateEmailRequest = new AssociationRegisterBuilder()
                .withEmail(request.email())
                .withPhoneNumber("+34660000111")
                .withName("NewAssociation")
                .withPassword("StrongPassw0rd!").withConfirmPassword("StrongPassw0rd!")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateEmailRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Failed to save association")));
    }

    @Test
    void shouldFailIfPhoneNumberIsAlreadyUsed() throws Exception {
        AssociationRegister request = validBuilder.build();
        performSuccessfulRegister(request);

        AssociationRegister duplicatePhoneRequest = new AssociationRegisterBuilder()
                .withPhoneNumber(request.phoneNumber())
                .withEmail("uniqueemail@example.com")
                .withName("AnotherAssociation")
                .withPassword("StrongPassw0rd!").withConfirmPassword("StrongPassw0rd!")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicatePhoneRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Failed to save association")));
    }

    @Test
    void shouldFailIfAssociationNameIsAlreadyUsed() throws Exception {
        AssociationRegister request = validBuilder.build();
        performSuccessfulRegister(request);

        AssociationRegister duplicateNameRequest = new AssociationRegisterBuilder()
                .withName(request.name())
                .withEmail("anotheremail@example.com")
                .withPhoneNumber("+34660000222")
                .withPassword("StrongPassw0rd!").withConfirmPassword("StrongPassw0rd!")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateNameRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Failed to save association")));
    }

    @Test
    void shouldFailIfUserNameIsAlreadyUsed() throws Exception {
        AssociationRegister request = validBuilder.build();
        performSuccessfulRegister(request);

        AssociationRegister duplicateUserNameRequest = new AssociationRegisterBuilder()
                .withEmail(request.email())
                .withPhoneNumber("+34660000333")
                .withName("UniqueNewAssociation")
                .withPassword("StrongPassw0rd!").withConfirmPassword("StrongPassw0rd!")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUserNameRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Failed to save association")));
    }

    private void performSuccessfulRegister(AssociationRegister request) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}