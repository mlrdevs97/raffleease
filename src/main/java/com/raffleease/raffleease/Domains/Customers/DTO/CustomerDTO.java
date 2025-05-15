package com.raffleease.raffleease.Domains.Customers.DTO;

import com.raffleease.raffleease.Domains.Customers.Model.CustomerSourceType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CustomerDTO(
        String stripeId,
        CustomerSourceType sourceType,
        String fullName,
        String email,
        String phoneNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}