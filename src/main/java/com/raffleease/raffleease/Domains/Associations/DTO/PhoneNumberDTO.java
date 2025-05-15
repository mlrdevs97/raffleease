package com.raffleease.raffleease.Domains.Associations.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneNumberDTO(
        @NotBlank
        @Pattern(regexp = "^\\+\\d{1,3}")
        String prefix,

        @NotBlank
        @Pattern(regexp = "^\\d{1,14}$")
        String nationalNumber
) { }
