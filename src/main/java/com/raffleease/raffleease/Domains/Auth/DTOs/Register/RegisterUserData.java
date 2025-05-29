package com.raffleease.raffleease.Domains.Auth.DTOs.Register;

import com.raffleease.raffleease.Common.Validations.PasswordMatches;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import static com.raffleease.raffleease.Common.Utils.SanitizeUtils.trim;
import static com.raffleease.raffleease.Common.Utils.SanitizeUtils.trimAndLower;

@Builder
@PasswordMatches
public record RegisterUserData(
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
        public RegisterUserData {
                firstName = trim(firstName);
                lastName = trim(lastName);
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