package com.raffleease.raffleease.Domains.Raffles.DTOs;

import lombok.Builder;

@Builder
public record OrderRaffleSummary(
        Long id,
        String title,
        String imageURL
) {}
