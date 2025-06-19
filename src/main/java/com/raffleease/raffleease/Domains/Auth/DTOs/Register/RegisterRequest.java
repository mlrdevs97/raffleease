package com.raffleease.raffleease.Domains.Auth.DTOs.Register;

import com.raffleease.raffleease.Common.Models.CreateUserData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RegisterRequest(
        @NotNull
        @Valid
        CreateUserData userData,

        @NotNull
        @Valid
        RegisterAssociationData associationData
) {}
