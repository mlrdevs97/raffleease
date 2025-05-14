package com.raffleease.raffleease.Domains.Auth.DTOs.Register;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RegisterRequest(
        @NotNull
        @Valid
        RegisterUserData userData,

        @NotNull
        @Valid
        RegisterAssociationData associationData
) {}
