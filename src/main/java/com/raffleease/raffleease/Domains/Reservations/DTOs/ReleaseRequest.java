package com.raffleease.raffleease.Domains.Reservations.DTOs;

import lombok.Builder;

import java.util.List;

@Builder
public record ReleaseRequest(
        List<Long> ticketIds
) {
}
