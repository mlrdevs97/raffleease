package com.raffleease.raffleease.Domains.Orders.DTOs;

import com.raffleease.raffleease.Domains.Orders.Model.OrderSource;
import com.raffleease.raffleease.Domains.Orders.Model.OrderStatus;
import com.raffleease.raffleease.Domains.Payments.Model.PaymentMethod;
import com.raffleease.raffleease.Domains.Payments.Model.PaymentStatus;
import com.raffleease.raffleease.Helpers.SanitizeUtils;
import com.raffleease.raffleease.Validations.ValidOrderSearchFilters;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.raffleease.raffleease.Helpers.SanitizeUtils.trim;
import static com.raffleease.raffleease.Helpers.SanitizeUtils.trimAndLower;

@ValidOrderSearchFilters
public record OrderSearchFilters(
        OrderStatus status,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        OrderSource orderSource,
        String orderReference,

        @Size(max = 100, message = "Customer name must not exceed 100 characters")
        String customerName,

        @Size(max = 100, message = "Customer email must not exceed 100 characters")
        String customerEmail,

        @Size(max = 30, message = "Phone number must not exceed 30 characters")
        String customerPhone,

        @Positive(message = "Raffle ID must be a positive number")
        Long raffleId,

        @PositiveOrZero(message = "Minimum total must be 0 or more")
        BigDecimal minTotal,

        @PositiveOrZero(message = "Maximum total must be 0 or more")
        BigDecimal maxTotal,

        LocalDateTime createdFrom,
        LocalDateTime createdTo,
        LocalDateTime completedFrom,
        LocalDateTime completedTo,
        LocalDateTime cancelledFrom,
        LocalDateTime cancelledTo
) {
        public OrderSearchFilters {
                orderReference = trim(orderReference);
                customerName = trim(customerName);
                customerEmail = trimAndLower(customerEmail);
                customerPhone = trim(customerPhone);
        }
}
