package com.raffleease.raffleease.Domains.Raffles.DTOs;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record RaffleStatisticsDTO (
        Long id,
        Long raffleId,
        Long availableTickets,
        Long soldTickets,
        Long closedSells,
        Long failedSells,
        Long refundTickets,
        Long unpaidTickets,
        BigDecimal revenue
) {}