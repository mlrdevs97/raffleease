package com.raffleease.raffleease.Domains.Payments.DTOs;

import com.raffleease.raffleease.Domains.Payments.Model.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PaymentDTO(
        Long id,

        @NotBlank(message = "Payment Intent ID cannot be blank")
        @Size(max = 50, message = "Payment Intent ID cannot exceed 50 characters")
        String paymentIntentId,

        @NotNull(message = "Payment status is required")
        PaymentStatus status,

        @NotBlank(message = "Payment method required")
        @Size(max = 30, message = "Payment method cannot exceed 30 characters")
        String paymentMethod,

        @NotNull(message = "Total cannot be null")
        @Positive(message = "Total must be positive")
        BigDecimal total,

        LocalDateTime createdAt,

        LocalDateTime completedAt
) {
}
