package com.raffleease.raffleease.Domains.Payments.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record SessionCreate (
    @NotNull(message = "Raffle ID cannot be null")
    @Min(value = 1, message = "Raffle ID must be greater than 0")
    Long raffleId,

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be greater than 0")
    Long quantity,

    @NotNull(message = "Order ID cannot be null")
    @Min(value = 1, message = "Order ID must be greater than 0")
    Long orderId
) {}
