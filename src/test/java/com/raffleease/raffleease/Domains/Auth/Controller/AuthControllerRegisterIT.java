package com.raffleease.raffleease.Domains.Auth.Controller;

import com.raffleease.raffleease.Configs.CorsProperties;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationMembership;
import com.raffleease.raffleease.Domains.Associations.Repository.AssociationsMembershipsRepository;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.PhoneNumberData;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterRequest;
import com.raffleease.raffleease.Domains.Auth.Model.VerificationToken;
import com.raffleease.raffleease.Domains.Notifications.Services.EmailsService;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Helpers.RegisterBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerRegisterIT extends BaseAuthIT {
    @Autowired
    private AssociationsMembershipsRepository membershipsRepository;

    @Autowired
    private CorsProperties corsProperties;

    private RegisterBuilder validBuilder;

    @BeforeEach
    void setUp() {
        validBuilder = new RegisterBuilder();
    }

    @MockitoBean
    private EmailsService emailsService;

    @Test
    void shouldRegisterAndCreateUnverifiedUserAndSendVerificationEmail() throws Exception {
        RegisterRequest request = validBuilder.build();
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("New association account created successfully"));

        // -- USER assertions --
        String expectedUserName = request.userData().userName().trim().toLowerCase();
        Optional<User> optionalUser = usersRepository.findByIdentifier(request.userData().email().trim());
        assertThat(optionalUser).isPresent();
        User user = optionalUser.get();

        assertThat(user.isEnabled()).isFalse();
        assertThat(user.getFirstName()).isEqualTo(request.userData().firstName().trim());
        assertThat(user.getLastName()).isEqualTo(request.userData().lastName().trim());
        assertThat(user.getUserName()).isEqualTo(expectedUserName);
        assertThat(user.getEmail()).isEqualTo(request.userData().email().trim());
        assertThat(user.getPhoneNumber()).isEqualTo(
                request.userData().phoneNumber().prefix().trim() + request.userData().phoneNumber().nationalNumber().trim()
        );

        // Username and phone should be usable as identifiers
        assertThat(usersRepository.findByIdentifier(expectedUserName)).isPresent();
        assertThat(usersRepository.findByIdentifier(user.getPhoneNumber())).isPresent();

        // -- ASSOCIATION assertions --
        List<Association> associations = associationsRepository.findAll();
        assertThat(associations.size()).isEqualTo(1);
        Association association = associations.get(0);

        assertThat(association.getName()).isEqualTo(request.associationData().associationName().trim());
        assertThat(association.getEmail()).isEqualTo(request.associationData().email().trim());
        assertThat(association.getDescription()).isEqualTo(request.associationData().description().trim());
        assertThat(association.getPhoneNumber()).isEqualTo(
                request.associationData().phoneNumber().prefix().trim() + request.associationData().phoneNumber().nationalNumber().trim()
        );
        assertThat(association.getAddress().getPlaceId()).isEqualTo(request.associationData().addressData().placeId());
        assertThat(association.getAddress().getFormattedAddress()).isEqualTo(request.associationData().addressData().formattedAddress());

        // Membership
        List<AssociationMembership> memberships = membershipsRepository.findAll();
        assertThat(memberships.size()).isEqualTo(1);
        assertThat(memberships.get(0).getUser().getId()).isEqualTo(user.getId());

        // -- VERIFICATION TOKEN --
        Optional<VerificationToken> tokenOpt = verificationTokenRepository.findByUser(user);
        assertThat(tokenOpt).isPresent();
        VerificationToken token = tokenOpt.get();

        assertThat(token.getToken()).isNotBlank();
        assertThat(token.getExpiryDate()).isAfter(LocalDateTime.now());

        // -- EMAIL SENT --
        String expectedLink = UriComponentsBuilder.fromHttpUrl(corsProperties.getClientAsList().get(0))
                .path("/admin/auth/verify-email")
                .queryParam("token", token.getToken())
                .build()
                .toUriString();
        verify(emailsService).sendEmailVerificationEmail(refEq(user), eq(expectedLink));
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
        performRegisterRequest(validBuilder.build()); // first registration

        RegisterRequest duplicate = new RegisterBuilder()
                .withUserEmail("new@mail.com")
                .withUserPhone("+34", "600999888")
                .withAssociationName("Another")
                .withAssociationEmail("another@assoc.com")
                .withAssociationPhone("+34", "600111122")
                .build();

        performRegisterRequest(duplicate)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors['userData.userName']").value("This value is already in use"));
    }

    @Test
    void shouldFailWhenUserEmailAlreadyExists() throws Exception {
        performRegisterRequest(validBuilder.build());

        RegisterRequest duplicate = new RegisterBuilder()
                .withUserName("anotheruser")
                .withUserPhone("+34", "600999888")
                .withAssociationName("Another")
                .withAssociationEmail("another@assoc.com")
                .withAssociationPhone("+34", "600111122")
                .build();

        performRegisterRequest(duplicate)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors['userData.email']").value("This value is already in use"));
    }

    @Test
    void shouldFailWhenUserPhoneNumberAlreadyExists() throws Exception {
        RegisterRequest original = validBuilder.build();
        performRegisterRequest(original);

        PhoneNumberData phone = original.userData().phoneNumber();

        RegisterRequest duplicate = new RegisterBuilder()
                .withUserEmail("unique@mail.com")
                .withUserName("new_user")
                .withUserPhone(phone.prefix(), phone.nationalNumber())
                .withAssociationName("Another")
                .withAssociationEmail("another@assoc.com")
                .withAssociationPhone("+34", "600111122")
                .build();

        performRegisterRequest(duplicate)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors['userData.phoneNumber']").value("This value is already in use"));
    }

    @Test
    void shouldFailWhenAssociationNameAlreadyExists() throws Exception {
        performRegisterRequest(validBuilder.build());

        RegisterRequest duplicate = new RegisterBuilder()
                .withUserEmail("unique@mail.com")
                .withUserName("another")
                .withUserPhone("+34", "600111199")
                .withAssociationEmail("new@assoc.com")
                .withAssociationPhone("+34", "600111122")
                .build();

        performRegisterRequest(duplicate)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors['associationData.associationName']").value("This value is already in use"));
    }

    @Test
    void shouldFailWhenAssociationEmailAlreadyExists() throws Exception {
        performRegisterRequest(validBuilder.build());

        RegisterRequest duplicate = new RegisterBuilder()
                .withUserEmail("unique@mail.com")
                .withUserName("another")
                .withUserPhone("+34", "600111199")
                .withAssociationName("Another")
                .withAssociationPhone("+34", "600111122")
                .build();

        performRegisterRequest(duplicate)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors['associationData.email']").value("This value is already in use"));
    }

    @Test
    void shouldFailWhenAssociationPhoneAlreadyExists() throws Exception {
        RegisterRequest original = validBuilder.build();
        performRegisterRequest(original);

        PhoneNumberData phone = original.associationData().phoneNumber();

        RegisterRequest duplicate = new RegisterBuilder()
                .withUserEmail("unique@mail.com")
                .withUserName("another")
                .withUserPhone("+34", "600111199")
                .withAssociationName("Another")
                .withAssociationEmail("another@assoc.com")
                .withAssociationPhone(phone.prefix(), phone.nationalNumber())
                .build();

        performRegisterRequest(duplicate)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors['associationData.phoneNumber']").value("This value is already in use"));
    }

    @Test
    void shouldFailWithFirstUniqueViolationAmongMany() throws Exception {
        RegisterRequest original = validBuilder.build();
        performRegisterRequest(original);

        // Attempt to violate multiple constraints at once
        RegisterRequest duplicate = new RegisterBuilder()
                .withUserEmail(original.userData().email())
                .withUserName(original.userData().userName())
                .withUserPhone(original.userData().phoneNumber().prefix(), original.userData().phoneNumber().nationalNumber())
                .withAssociationName(original.associationData().associationName())
                .withAssociationEmail(original.associationData().email())
                .withAssociationPhone(original.associationData().phoneNumber().prefix(), original.associationData().phoneNumber().nationalNumber()) // duplicated
                .build();

        performRegisterRequest(duplicate)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors").isMap()) // still one error
                .andExpect(jsonPath("$.errors['userData.email']").value("This value is already in use"));
    }

    @Test
    void shouldTrimAndNormalizeUserAndAssociationFieldsOnRegister() throws Exception {
        RegisterBuilder builder = new RegisterBuilder()
                .withUserFirstName("  Firstname  ")
                .withUserLastName("  Last Name  ")
                .withUserName("  Test_User  ")
                .withUserEmail("  user@example.com  ")
                .withUserPhone(" +34 ", " 600001111 ")
                .withAssociationName("  Test Association ")
                .withAssociationEmail("  association@example.com ")
                .withAssociationPhone(" +34 ", " 600009999 ")
                .withDescription("  Helping the world!  ");

        RegisterRequest request = builder.build();

        performRegisterRequest(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("New association account created successfully"));

        String expectedUserName = builder.getUserName().trim().toLowerCase();
        Optional<User> optionalUser = usersRepository.findByIdentifier(expectedUserName);
        assertThat(optionalUser).isPresent();
        User user = optionalUser.get();

        assertThat(user.getFirstName()).isEqualTo(builder.getFirstName().trim());
        assertThat(user.getLastName()).isEqualTo(builder.getLastName().trim());
        assertThat(user.getUserName()).isEqualTo(expectedUserName);
        assertThat(user.getEmail()).isEqualTo(builder.getUserEmail().trim());
        assertThat(user.getPhoneNumber()).isEqualTo(builder.getUserPhonePrefix().trim() + builder.getUserPhoneNumber().trim());

        List<Association> associations = associationsRepository.findAll();
        assertThat(associations.size()).isEqualTo(1);
        Association association = associations.get(0);

        assertThat(association.getName()).isEqualTo(builder.getAssociationName().trim());
        assertThat(association.getEmail()).isEqualTo(builder.getAssociationEmail().trim());
        assertThat(association.getPhoneNumber())
                .isEqualTo(builder.getAssociationPhonePrefix().trim() + builder.getAssociationPhoneNumber().trim());
        assertThat(association.getDescription()).isEqualTo(builder.getDescription().trim());
    }

}