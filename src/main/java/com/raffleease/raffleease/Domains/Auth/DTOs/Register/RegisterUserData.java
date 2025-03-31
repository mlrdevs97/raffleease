package com.raffleease.raffleease.Domains.Auth.DTOs.Register;

import com.raffleease.raffleease.Validations.PasswordMatches;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
@PasswordMatches
public record RegisterUserData(
        @NotBlank(message = "Association's name is required")
        @Size(min = 2, max = 25, message = "Name must be between 2 and 25 characters")
        String userName,

        @NotBlank(message = "Association's email is required")
        @Email(message = "Must provide a valid email")
        String email,

        @Nullable
        @Valid
        RegisterPhoneNumber phoneNumber,

        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,32}$",
                message = "Password must be between 8 and 32 characters, and include at least one lowercase letter, one uppercase letter, one digit, and one special character."
        )
        String password,

        @NotBlank(message = "Confirm password is required")
        String confirmPassword
) { }