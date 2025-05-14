package com.raffleease.raffleease.Domains.Auth.DTOs.Register;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import static com.raffleease.raffleease.Helpers.SanitizeUtils.trim;

@Builder
public record PhoneNumberData(
        @NotBlank
        @Pattern(regexp = "^\\+\\d{1,3}")
        String prefix,

        @NotBlank
        @Pattern(regexp = "^\\d{1,14}$")
        String nationalNumber
) {
        public PhoneNumberData {
                prefix = trim(prefix);
                nationalNumber = trim(nationalNumber);
        }
}
