package com.raffleease.raffleease.Common.Models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import static com.raffleease.raffleease.Common.Utils.SanitizeUtils.trim;
import static com.raffleease.raffleease.Common.Constants.ValidationPatterns.*;

/**
 * Shared phone number model used across multiple domains.
 * Provides validation and sanitization for international phone numbers.
 */
@Builder
public record PhoneNumber(
        @NotBlank
        @Pattern(regexp = PHONE_PREFIX_PATTERN, message = Messages.PHONE_PREFIX_MESSAGE)
        String prefix,

        @NotBlank
        @Pattern(regexp = PHONE_NATIONAL_NUMBER_PATTERN, message = Messages.PHONE_NATIONAL_NUMBER_MESSAGE)
        String nationalNumber
) {
    public PhoneNumber {
        prefix = trim(prefix);
        nationalNumber = trim(nationalNumber);
    }
}