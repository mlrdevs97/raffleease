package com.raffleease.raffleease.Domains.Payments.DTOs;

import com.raffleease.raffleease.Domains.Payments.Model.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PaymentDTO(
        PaymentStatus status,
        String paymentMethod,
        BigDecimal total,
        String paymentIntentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt,
        LocalDateTime cancelledAt
) {
}
