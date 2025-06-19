package com.raffleease.raffleease.Domains.Users.DTOs;

import com.raffleease.raffleease.Common.Validations.PasswordMatches;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.PhoneNumberData;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import static com.raffleease.raffleease.Common.Utils.SanitizeUtils.trim;
import static com.raffleease.raffleease.Common.Utils.SanitizeUtils.trimAndLower;

@Builder
@PasswordMatches
public record CreateUserData(
        @NotBlank
        @Size(min = 2, max = 50)
        String firstName,

        @NotBlank
        @Size(min = 2, max = 50)
        String lastName,

        @NotBlank
        @Size(min = 2, max = 25)
        String userName,

        @NotBlank
        @Email
        String email,

        @Nullable
        @Valid
        PhoneNumberData phoneNumber,

        @NotBlank
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%-^&_*(),.?\":{}|<>]).{8,32}$")
        String password,

        @NotBlank
        String confirmPassword
) {
        public CreateUserData {
                firstName = trimAndLower(firstName);
                lastName = trimAndLower(lastName);
                userName = trimAndLower(userName);
                email = trimAndLower(email);
                password = trim(password);
                confirmPassword = trim(confirmPassword);
                phoneNumber = phoneNumber == null ? null : new PhoneNumberData(
                        trim(phoneNumber.prefix()),
                        trim(phoneNumber.nationalNumber())
                );
        }
} 