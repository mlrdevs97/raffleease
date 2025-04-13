package com.raffleease.raffleease.Domains.Associations.DTO;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.PhoneNumberData;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterAddressData;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.springframework.validation.annotation.Validated;

@Builder
public record AssociationDTO (
        @NotNull(message = "Association ID is required")
        Long id,

        @NotBlank(message = "Google Place ID is required")
        String placeId,

        @NotBlank(message = "Association's name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String associationName,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description,

        @Email(message = "Must provide a valid email")
        String email,

        @NotBlank(message = "Must provide a phone number")
        @Pattern(regexp = "^\\+\\d{1,3}\\d{1,14}$", message = "Must provide a valid phone number")
        String phoneNumber,

        @NotNull(message = "Must provide address data")
        @Valid
        AddressDTO addressData
) {}