package com.raffleease.raffleease.Domains.Auth.DTOs.Register;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RegisterRequest(
        @NotNull(message = "Must provide user data")
        @Valid
        RegisterUserData userData,

        @NotNull(message = "Must provide association data")
        @Valid
        RegisterAssociationData associationData
) {}
