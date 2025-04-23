package com.raffleease.raffleease.Domains.Tickets.DTO;

import com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus;
import lombok.Builder;


import lombok.Builder;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record TicketDTO(
        Long id,
        String ticketNumber,
        TicketStatus status,
        Long raffleId,
        Long customerId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

