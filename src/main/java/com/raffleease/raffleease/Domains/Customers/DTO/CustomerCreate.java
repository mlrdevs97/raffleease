package com.raffleease.raffleease.Domains.Customers.DTO;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.PhoneNumberData;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CustomerCreate(
        @NotBlank(message = "Must provide a name for user")
        String fullName,

        @Nullable
        @Email(message = "Must provide a valid email")
        String email,

        @Nullable
        @Valid
        PhoneNumberData phoneNumber
) {
}
