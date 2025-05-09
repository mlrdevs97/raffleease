package com.raffleease.raffleease.Domains.Auth.Controller;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.PhoneNumberData;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterRequest;
import com.raffleease.raffleease.Domains.Auth.Model.VerificationToken;
import com.raffleease.raffleease.Domains.Notifications.Services.EmailsService;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Helpers.RegisterBuilder;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerRegisterIT extends BaseAuthIT {
    private RegisterBuilder validBuilder;

    @BeforeEach
    void setUp() {
        validBuilder = new RegisterBuilder();
    }

    @MockitoBean
    private EmailsService emailsService;

    @Value("${spring.application.host.client}")
    private String clientHost;

    @Test
    @Transactional
    void shouldRegisterAndCreateUnverifiedUserAndSendVerificationEmail() throws Exception {
        RegisterRequest request = validBuilder.build();
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("New association account created successfully"))
                .andReturn();

        // Assert user exists but is not enabled
        Optional<User> optionalUser = usersRepository.findByIdentifier(request.userData().email());
        assertThat(optionalUser).isPresent();
        User user = optionalUser.get();
        assertThat(user.isEnabled()).isFalse();

        // Assert association exists and membership was created
        String phoneNumber = request.userData().phoneNumber().prefix() + request.userData().phoneNumber().nationalNumber();
        assertThat(usersRepository.findByIdentifier(request.userData().userName())).isPresent();
        assertThat(usersRepository.findByIdentifier(phoneNumber)).isPresent();

        List<Association> associations = associationsRepository.findAll();
        assertThat(associations.size()).isEqualTo(1);
        Association association = associations.get(0);
        assertThat(association.getMemberships().size()).isEqualTo(1);
        assertThat(association.getMemberships().get(0).getUser().getId()).isEqualTo(user.getId());

        // Assert a verification token was created
        Optional<VerificationToken> tokenOpt = verificationTokenRepository.findByUser(user);
        assertThat(tokenOpt).isPresent();
        VerificationToken token = tokenOpt.get();
        assertThat(token.getToken()).isNotBlank();
        assertThat(token.getExpiryDate()).isAfter(LocalDateTime.now());

        // Assert verification email was sent
        String expectedLink = UriComponentsBuilder.fromHttpUrl(clientHost)
                .path("/admin/auth/verify-email")
                .queryParam("token", token.getToken())
                .build()
                .toUriString();
        verify(emailsService).sendEmailVerificationEmail(eq(user), eq(expectedLink));
    }

    @Test
    void shouldFailWhenUserNameIsNull() throws Exception {
        performRegisterRequest(validBuilder.withUserName(null).build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['userData.userName']").value("User name is required"));
    }

    @Test
    void shouldFailWhenUserNameDoesNotMatchLengthConstraint() throws Exception {
        performRegisterRequest(validBuilder.withUserName("A").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['userData.userName']").value("Name must be between 2 and 25 characters"));
    }

    @Test
    void shouldFailWhenUserEmailIsNull() throws Exception {
        performRegisterRequest(validBuilder.withUserEmail(null).build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['userData.email']").value("User email is required"));
    }

    @Test
    void shouldFailWhenUserEmailIsInvalid() throws Exception {
        performRegisterRequest(validBuilder.withUserEmail("invalid-email").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['userData.email']").value("Must provide a valid email"));
    }

    @Test
    void shouldRegisterWithoutPhoneNumber() throws Exception {
        performRegisterRequest(validBuilder.withUserPhone(null, null).build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("New association account created successfully"));
    }

    @Test
    void shouldFailWhenPhonePrefixIsNull() throws Exception {
        performRegisterRequest(validBuilder.withUserPhone(null, "600123456").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['userData.phoneNumber.prefix']").value("Must provide a prefix for phone number"));
    }

    @Test
    void shouldFailWhenPhoneNumberInvalid() throws Exception {
        performRegisterRequest(validBuilder.withUserPhone("+34", "abc").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['userData.phoneNumber.nationalNumber']").value("Must provide a valid phone number"));
    }

    @Test
    void shouldFailWhenAssociationPhonePrefixIsInvalid() throws Exception {
        performRegisterRequest(validBuilder.withAssociationPhone("-x", "600009999").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['associationData.phoneNumber.prefix']").value("Must provide a valid prefix"));
    }

    @Test
    void shouldFailWhenAssociationPhoneNumberIsInvalid() throws Exception {
        performRegisterRequest(validBuilder.withAssociationPhone("+34", "invalid").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['associationData.phoneNumber.nationalNumber']").value("Must provide a valid phone number"));
    }

    @Test
    void shouldFailWhenPasswordTooWeak() throws Exception {
        performRegisterRequest(validBuilder.withPassword("short").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['userData.password']").exists());
    }

    @Test
    void shouldFailWhenPasswordMismatch() throws Exception {
        performRegisterRequest(validBuilder.withConfirmPassword("Different123!").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['userData.confirmPassword']").value("Password and confirm password don't match"));
    }

    @Test
    void shouldFailWhenAssociationNameIsNull() throws Exception {
        performRegisterRequest(validBuilder.withAssociationName(null).build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['associationData.associationName']").value("Association's name is required"));
    }

    @Test
    void shouldFailWhenDescriptionTooLong() throws Exception {
        performRegisterRequest(validBuilder.withDescription("a".repeat(501)).build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['associationData.description']").exists());
    }

    @Test
    void shouldFailWhenAssociationEmailInvalid() throws Exception {
        performRegisterRequest(validBuilder.withAssociationEmail("invalid").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['associationData.email']").value("Must provide a valid email"));
    }

    @Test
    void shouldFailWhenAddressPlaceIdIsNull() throws Exception {
        performRegisterRequest(validBuilder.withAddress(null, "Address", 40.0, -3.0, "Madrid", "Madrid", "28001").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['associationData.addressData.placeId']").value("Google Place ID is required"));
    }

    @Test
    void shouldFailWhenAddressFormattedAddressIsNull() throws Exception {
        performRegisterRequest(validBuilder.withAddress("placeId", null, 40.0, -3.0, "Madrid", "Madrid", "28001").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['associationData.addressData.formattedAddress']").value("Formatted address is required"));
    }

    @Test
    void shouldFailWhenLatitudeIsNull() throws Exception {
        performRegisterRequest(validBuilder.withAddress("placeId", "Address", null, -3.0, "Madrid", "Madrid", "28001").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['associationData.addressData.latitude']").exists());
    }

    @Test
    void shouldFailWhenLongitudeIsNull() throws Exception {
        performRegisterRequest(validBuilder.withAddress("placeId", "Address", 40.0, null, "Madrid", "Madrid", "28001").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['associationData.addressData.longitude']").exists());
    }

    @Test
    void shouldFailWhenLatitudeIsOutOfRange() throws Exception {
        performRegisterRequest(validBuilder.withAddress("placeId", "Address", -91.0, -3.0, "Madrid", "Madrid", "28001").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['associationData.addressData.latitude']").value("Latitude must be between -90 and 90"));
    }

    @Test
    void shouldFailWhenLongitudeIsOutOfRange() throws Exception {
        performRegisterRequest(validBuilder.withAddress("placeId", "Address", 40.0, -181.0, "Madrid", "Madrid", "28001").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['associationData.addressData.longitude']").value("Longitude must be between -180 and 180"));
    }

    @Test
    void shouldFailWhenCityIsNull() throws Exception {
        performRegisterRequest(validBuilder.withAddress("placeId", "Address", 40.0, -3.0, null, "Madrid", "28001").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['associationData.addressData.city']").value("City is required"));
    }

    @Test
    void shouldFailWhenProvinceTooShort() throws Exception {
        performRegisterRequest(validBuilder.withAddress("placeId", "Address", 40.0, -3.0, "Madrid", "A", "28001").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['associationData.addressData.province']").value("Province must be between 2 and 100 characters"));
    }

    @Test
    void shouldFailWhenZipCodeInvalid() throws Exception {
        performRegisterRequest(validBuilder.withAddress("placeId", "Address", 40.0, -3.0, "Madrid", "Madrid", "123").build())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['associationData.addressData.zipCode']").value("Must provide a valid zip code"));
    }

    @Test
    void shouldFailIfPasswordDoesNotMeetRegex() throws Exception {
        RegisterRequest request = validBuilder.withPassword("weakpass").withConfirmPassword("weakpass").build();

        performRegisterRequest(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['userData.password']").value(containsString("Password must be between 8 and 32 characters")));
    }

    @Test
    void shouldFailIfPasswordIsMissing() throws Exception {
        RegisterRequest request = validBuilder.withPassword(null).build();

        performRegisterRequest(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['userData.password']").value("Password is required"));
    }

    @Test
    void shouldFailIfConfirmPasswordIsMissing() throws Exception {
        RegisterRequest request = validBuilder.withConfirmPassword(null).build();

        performRegisterRequest(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['userData.confirmPassword']").exists());
    }

    @Test
    void shouldFailIfPasswordDoesNotMatchConfirmPassword() throws Exception {
        RegisterRequest request = validBuilder.withPassword("MySecurePassword#123").withConfirmPassword("SecurePassword#123").build();

        performRegisterRequest(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['userData.confirmPassword']").value("Password and confirm password don't match"));
    }

    @Test
    void shouldFailWhenUserNameAlreadyExists() throws Exception {
        RegisterRequest original = validBuilder.build();
        performRegisterRequest(original);

        RegisterRequest duplicate = new RegisterBuilder()
                .withUserEmail("random@mail.com")
                .withUserPhone("+34", "600111111")
                .withAssociationName("Another Association")
                .withAssociationEmail("another@example.com")
                .withAssociationPhone("+34", "600222222")
                .withAddress("randomPlaceId", "Address", 40.0, -80.0, "Madrid", "Madrid", "28001")
                .withPassword("MySecurePassword#123")
                .withConfirmPassword("MySecurePassword#123")
                .build();

        performRegisterRequest(duplicate)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Failed to save user")));
    }

    @Test
    void shouldFailWhenUserEmailAlreadyExists() throws Exception {
        RegisterRequest original = validBuilder.build();
        performRegisterRequest(original);

        RegisterRequest duplicate = new RegisterBuilder()
                .withUserName("new_user")
                .withUserPhone("+34", "600111111")
                .withAssociationName("Another Association")
                .withAssociationEmail("another@example.com")
                .withAssociationPhone("+34", "600222222")
                .withAddress("randomPlaceId", "Address", 40.0, -81.0, "Madrid", "Madrid", "28001")
                .withPassword("MySecurePassword#123")
                .withConfirmPassword("MySecurePassword#123")
                .build();

        performRegisterRequest(duplicate)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Failed to save user")));
    }

    @Test
    void shouldFailWhenUserPhoneNumberAlreadyExists() throws Exception {
        RegisterRequest original = validBuilder.build();
        performRegisterRequest(original);

        PhoneNumberData phoneNumber = original.userData().phoneNumber();
        RegisterRequest duplicate = new RegisterBuilder()
                .withUserEmail("unexisting@email.com")
                .withUserName("new_user")
                .withAssociationName("Another Association")
                .withAssociationEmail("another@example.com")
                .withAssociationPhone(phoneNumber.prefix(), phoneNumber.nationalNumber())
                .withAddress("randomPlaceId", "Address", 40.0, -81.0, "Madrid", "Madrid", "28001")
                .withPassword("MySecurePassword#123")
                .withConfirmPassword("MySecurePassword#123")
                .build();

        performRegisterRequest(duplicate)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Failed to save user")));
    }

    @Test
    void shouldFailWhenAssociationNameAlreadyExists() throws Exception {
        RegisterRequest original = validBuilder.build();
        performRegisterRequest(original);

        RegisterRequest duplicate = new RegisterBuilder()
                .withUserEmail("unexisting@email.com")
                .withUserName("new_user")
                .withUserPhone("+34", "600111111")
                .withAssociationEmail("another@example.com")
                .withAssociationPhone("+34", "600222222")
                .withAddress("randomPlaceId", "Address", 40.0, -81.0, "Madrid", "Madrid", "28001")
                .withPassword("MySecurePassword#123")
                .withConfirmPassword("MySecurePassword#123")
                .build();

        performRegisterRequest(duplicate)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Failed to save association")));
    }

    @Test
    void shouldFailWhenAssociationEmailAlreadyExists() throws Exception {
        RegisterRequest original = validBuilder.build();
        performRegisterRequest(original);

        RegisterRequest duplicate = new RegisterBuilder()
                .withUserEmail("unexisting@mail.com")
                .withUserName("new_user")
                .withUserPhone("+34", "600111111")
                .withAssociationName("Another Association")
                .withAssociationPhone("+34", "600222222")
                .withAddress("randomPlaceId", "Address", 40.0, -81.0, "Madrid", "Madrid", "28001")
                .withPassword("MySecurePassword#123")
                .withConfirmPassword("MySecurePassword#123")
                .build();

        performRegisterRequest(duplicate)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Failed to save association")));
    }

    @Test
    void shouldFailWhenAssociationPhoneAlreadyExists() throws Exception {
        RegisterRequest original = validBuilder.build();
        performRegisterRequest(original);

        PhoneNumberData phoneNumber = original.associationData().phoneNumber();
        RegisterRequest duplicate = new RegisterBuilder()
                .withUserEmail("unexisting@mail.com")
                .withUserName("new_user")
                .withUserPhone("+34", "600111111")
                .withAssociationName("Another Association")
                .withAssociationEmail("another@example.com")
                .withAddress("randomPlaceId", "Address", 40.0, -81.0, "Madrid", "Madrid", "28001")
                .withPassword("MySecurePassword#123")
                .withConfirmPassword("MySecurePassword#123")
                .build();

        performRegisterRequest(duplicate)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Failed to save association")));
    }
}