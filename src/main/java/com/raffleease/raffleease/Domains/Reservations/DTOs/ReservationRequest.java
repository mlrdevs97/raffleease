package com.raffleease.raffleease.Domains.Reservations.DTOs;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record ReservationRequest (
        @NotNull(message = "Must indicate a raffle")
        Long raffleId,

        @NotEmpty(message = "Mus select at least one ticket")
        List<Long> ticketsIds
) { }