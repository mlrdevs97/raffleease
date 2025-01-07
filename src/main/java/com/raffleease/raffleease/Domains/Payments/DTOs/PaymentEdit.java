package com.raffleease.raffleease.Domains.Payments.DTOs;

import com.raffleease.raffleease.Domains.Payments.Model.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PaymentEdit(
        String paymentMethod,
        BigDecimal total,
        String paymentIntentId,
        PaymentStatus paymentStatus,
        LocalDateTime completedAt
) {
}
