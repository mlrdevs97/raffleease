package com.raffleease.raffleease.Domains.Associations.DTO;

import jakarta.validation.constraints.*;
import lombok.Builder;
import org.springframework.validation.annotation.Validated;

@Builder
public record AssociationDTO (
        @NotNull(message = "Association ID is required")
        Long id,

        @NotBlank(message = "Association name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Email(message = "Must provide a valid email")
        String email,

        @Pattern(regexp = "^\\+?[0-9]*$", message = "Must provide a valid phone number")
        String phoneNumber,

        @Validated
        AddressDTO address
) {}