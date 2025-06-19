package com.raffleease.raffleease.Domains.Users.DTOs;

import com.raffleease.raffleease.Domains.Users.Model.UserRole;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String userName,
        String email,
        String phoneNumber,
        UserRole userRole,
        boolean isEnabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
} 