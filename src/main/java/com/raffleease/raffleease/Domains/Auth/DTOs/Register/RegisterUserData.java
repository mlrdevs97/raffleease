package com.raffleease.raffleease.Domains.Auth.DTOs.Register;

import com.raffleease.raffleease.Validations.PasswordMatches;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import static com.raffleease.raffleease.Helpers.SanitizeUtils.trim;
import static com.raffleease.raffleease.Helpers.SanitizeUtils.trimAndLower;

@Builder
@PasswordMatches
public record RegisterUserData(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 100 characters")
        String lastName,

        @NotBlank(message = "User name is required")
        @Size(min = 2, max = 25, message = "Name must be between 2 and 25 characters")
        String userName,

        @NotBlank(message = "User email is required")
        @Email(message = "Must provide a valid email")
        String email,

        @Nullable
        @Valid
        PhoneNumberData phoneNumber,

        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,32}$",
                message = "Password must be between 8 and 32 characters, and include at least one lowercase letter, one uppercase letter, one digit, and one special character."
        )
        String password,

        @NotBlank(message = "Confirm password is required")
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