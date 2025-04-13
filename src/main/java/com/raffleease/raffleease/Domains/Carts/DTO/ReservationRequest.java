package com.raffleease.raffleease.Domains.Carts.DTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record ReservationRequest (
        @NotEmpty(message = "Must select at least one ticket")
        List<Long> ticketsIds
) { }