package com.raffleease.raffleease.Common.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.raffleease.raffleease.Common.Validations.PasswordMatches;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

import static com.raffleease.raffleease.Common.Constants.ValidationPatterns.PASSWORD_PATTERN;
import static com.raffleease.raffleease.Common.Constants.ValidationPatterns.Messages.PASSWORD_MESSAGE;
import static com.raffleease.raffleease.Common.Utils.SanitizeUtils.trim;
import static com.raffleease.raffleease.Common.Utils.SanitizeUtils.trimAndLower;

/**
 * Base user data model containing common fields for user registration and creation.
 * This model provides shared validation and structure for user-related DTOs.
 */
@Getter
@PasswordMatches
public class CreateUserData extends BaseUserData {
    @NotBlank
    @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
    private final String password;

    @NotBlank
    private final String confirmPassword;

    @JsonCreator
    public CreateUserData(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("userName") String userName,
            @JsonProperty("email") String email,
            @JsonProperty("phoneNumber") @Nullable PhoneNumber phoneNumber,
            @JsonProperty("password") String password,
            @JsonProperty("confirmPassword") String confirmPassword
    ) {
        super(firstName, lastName, userName, email, phoneNumber);
        this.password = trim(password);
        this.confirmPassword = trim(confirmPassword);
    }
}