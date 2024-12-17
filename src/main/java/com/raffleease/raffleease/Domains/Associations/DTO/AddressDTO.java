package com.raffleease.raffleease.Domains.Associations.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AddressDTO (
        Long id,

        @NotBlank(message = "Association's city is required")
        @Size(min = 2, max = 100, message = "Association's city name must be between 2 and 100 characters")
        String city,

        @NotBlank(message = "Association's province is required")
        String province,

        @NotBlank(message = "Association's zip code is required")
        @Pattern(regexp = "^[0-9]{5}(?:-[0-9]{4})?$", message = "Must provide a valid zip code")
        String zipCode
) {}
