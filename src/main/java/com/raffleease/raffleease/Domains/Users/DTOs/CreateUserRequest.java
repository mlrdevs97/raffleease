package com.raffleease.raffleease.Domains.Users.DTOs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateUserRequest(
        @NotNull
        @Valid
        CreateUserData userData
) {
} 