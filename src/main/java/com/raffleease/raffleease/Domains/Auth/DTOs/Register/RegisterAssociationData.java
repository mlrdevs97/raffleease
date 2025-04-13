package com.raffleease.raffleease.Domains.Auth.DTOs.Register;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record RegisterAssociationData(
        @NotBlank(message = "Association's name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String associationName,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description,

        @Email(message = "Must provide a valid email")
        String email,

        @Nullable
        @Valid
        PhoneNumberData phoneNumber,

        @NotNull(message = "Must provide address data")
        @Valid
        RegisterAddressData addressData
) { }