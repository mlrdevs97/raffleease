package com.raffleease.raffleease.Domains.Customers.DTO;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CustomerDTO(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}