package com.raffleease.raffleease.Domains.Associations.DTO;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record AddressDTO(
        @NotBlank(message = "Google Place ID is required")
        String placeId,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", inclusive = true, message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", inclusive = true, message = "Latitude must be between -90 and 90")
        Double latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", inclusive = true, message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", inclusive = true, message = "Longitude must be between -180 and 180")
        Double longitude,

        @NotBlank(message = "City is required")
        @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
        String city,

        @Size(min = 2, max = 100, message = "Province must be between 2 and 100 characters")
        String province,

        @Pattern(regexp = "^$|^[0-9]{5}(?:-[0-9]{4})?$", message = "Must provide a valid zip code")
        String zipCode,

        @NotBlank(message = "Formatted address is required")
        @Size(min = 5, max = 255, message = "Formatted address must be between 5 and 255 characters")
        String formattedAddress
) {}