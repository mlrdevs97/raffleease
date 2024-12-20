package com.raffleease.raffleease.Domains.Payments.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PaymentDTO (
        @Size(max = 50, message = "Payment ID cannot exceed 50 characters")
        Long paymentId,

        @NotNull(message = "Order ID cannot be null")
        Long orderId,

        @NotBlank(message = "Payment method required")
        @Size(max = 30, message = "Payment method cannot exceed 30 characters")
        String paymentMethod,

        @NotNull(message = "Total cannot be null")
        @Positive(message = "Total must be positive")
        BigDecimal total,

        @NotBlank(message = "Payment Intent ID cannot be blank")
        @Size(max = 50, message = "Payment Intent ID cannot exceed 50 characters")
        String paymentIntentId
) {}
