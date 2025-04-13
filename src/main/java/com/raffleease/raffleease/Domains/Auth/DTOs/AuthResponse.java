package com.raffleease.raffleease.Domains.Auth.DTOs;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        AssociationDTO association
) { }
