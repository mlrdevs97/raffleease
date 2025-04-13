package com.raffleease.raffleease.Domains.Customers.DTO;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.PhoneNumberData;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerCreate(
        @NotBlank(message = "User name is required")
        @Size(min = 5, max = 25, message = "Name must be between 5 and 25 characters")
        String fullName,

        @Nullable
        @Email(message = "Must provide a valid email")
        String email,

        @Nullable
        @Valid
        PhoneNumberData phoneNumber
) {
}
