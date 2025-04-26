package com.raffleease.raffleease.Domains.Auth.DTOs;

import jakarta.validation.constraints.NotBlank;

public record RegisterEmailVerificationRequest(
        @NotBlank(message = "Must provide a verification token")
        String verificationToken
) {
}
