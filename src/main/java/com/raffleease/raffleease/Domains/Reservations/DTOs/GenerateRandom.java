package com.raffleease.raffleease.Domains.Reservations.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GenerateRandom(
        @NotNull(message = "Raffle Id is required")
        Long raffleId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Long quantity
) {}