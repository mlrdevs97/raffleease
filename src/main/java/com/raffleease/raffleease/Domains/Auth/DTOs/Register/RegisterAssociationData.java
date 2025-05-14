package com.raffleease.raffleease.Domains.Auth.DTOs.Register;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import static com.raffleease.raffleease.Helpers.SanitizeUtils.trim;
import static com.raffleease.raffleease.Helpers.SanitizeUtils.trimAndLower;

@Builder
public record RegisterAssociationData(
        @NotBlank
        @Size(min = 2, max = 100)
        String associationName,

        @Size(max = 500)
        String description,

        @Email
        String email,

        @Nullable
        @Valid
        PhoneNumberData phoneNumber,

        @NotNull
        @Valid
        RegisterAddressData addressData
) {
        public RegisterAssociationData {
                associationName = trim(associationName);
                description = trim(description);
                email = trimAndLower(email);
                phoneNumber = phoneNumber == null ? null : new PhoneNumberData(
                        trim(phoneNumber.prefix()),
                        trim(phoneNumber.nationalNumber())
                );
        }
}