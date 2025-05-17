package com.raffleease.raffleease.Domains.Raffles.DTOs;

import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;

public record RaffleSearchFilters(
    String title,
    RaffleStatus status,
    String sortBy,
    String sortDirection
) {
    public RaffleSearchFilters {
        title = title != null ? title.trim() : null;
        sortBy = sortBy != null ? sortBy.trim() : null;
        sortDirection = sortDirection != null ? sortDirection.trim().toLowerCase() : null;
    }
} 