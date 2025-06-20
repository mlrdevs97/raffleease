package com.raffleease.raffleease.Domains.Users.DTOs;

import com.raffleease.raffleease.Common.Models.CreateUserData;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationRole;
import com.raffleease.raffleease.Domains.Users.Validations.ValidUserRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateUserRequest(
        @NotNull
        @Valid
        CreateUserData userData,
        
        @NotNull
        @ValidUserRole
        AssociationRole role
) {
} 