package com.raffleease.raffleease.Domains.Auth.DTOs;

import com.raffleease.raffleease.Validations.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@PasswordMatches
public record AssociationRegister(
        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,16}$",
                message = "Password must be between 8 and 16 characters, and include at least one lowercase letter, one uppercase letter, one digit, and one special character."
        )
        String password,

        @NotBlank(message = "Confirm password is required")
        String confirmPassword,

        @NotBlank(message = "Association's name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Association's email is required")
        @Email(message = "Must provide a valid email")
        String email,

        @Pattern(regexp = "^\\+?[0-9]*$", message = "Must provide a valid phone number")
        String phoneNumber,

        @NotBlank(message = "Association's city is required")
        @Size(min = 2, max = 100, message = "City name must be between 2 and 100 characters")
        String city,

        @NotBlank(message = "Association's province is required")
        String province,

        @NotBlank(message = "Association's zip code required")
        @Pattern(regexp = "^[0-9]{5}(?:-[0-9]{4})?$", message = "Must provide a valid zip code")
        String zipCode
) {
}
