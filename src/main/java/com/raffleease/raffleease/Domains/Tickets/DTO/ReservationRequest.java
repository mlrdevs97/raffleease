package com.raffleease.raffleease.Domains.Tickets.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Set;

@Builder
public record ReservationRequest (
        @NotNull(message = "Must indicate a raffle")
        Long raffleId,

        @NotEmpty(message = "Mus select at least one ticket")
        Set<Long> ticketsIds
) { }