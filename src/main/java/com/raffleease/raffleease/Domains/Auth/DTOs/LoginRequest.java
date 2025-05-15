package com.raffleease.raffleease.Domains.Auth.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LoginRequest(
        @NotBlank
        String identifier,

        @NotBlank
        String password
) {}
