package com.raffleease.raffleease.Domains.Raffles.DTOs;

import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdate(
        @NotNull(message = "Must provide a new status for raffle")
        RaffleStatus status
) { }
