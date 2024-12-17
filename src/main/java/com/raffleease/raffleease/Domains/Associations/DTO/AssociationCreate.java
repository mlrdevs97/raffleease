package com.raffleease.raffleease.Domains.Associations.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AssociationCreate (
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