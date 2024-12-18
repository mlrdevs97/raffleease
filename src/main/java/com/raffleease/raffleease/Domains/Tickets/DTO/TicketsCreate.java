package com.raffleease.raffleease.Domains.Tickets.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TicketsCreate (
    @NotNull(message = "Must indicate a tickets amount for raffle")
    @Positive(message = "Tickets amount must be greater than zero")
    Long amount,

    @NotNull(message = "Must indicate a price for tickets in raffle")
    @Positive(message = "Price must be greater than zero")
    BigDecimal price,

    @NotNull(message = "Must indicate a lower limit for the tickets")
    @Min(value = 0, message = "Lower limit must be greater than or equal to zero")
    Long lowerLimit
){}
