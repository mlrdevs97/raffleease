package com.raffleease.raffleease.Domains.Users.DTOs;

import com.raffleease.raffleease.Common.Models.BaseUserData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record EditUserRequest(
        @NotNull
        @Valid
        BaseUserData userData
) { }
