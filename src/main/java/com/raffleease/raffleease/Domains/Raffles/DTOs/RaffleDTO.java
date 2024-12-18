package com.raffleease.raffleease.Domains.Raffles.DTOs;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record RaffleDTO(
        Long id,

        String title,

        String description,

        String url,

        LocalDateTime startDate,

        LocalDateTime endDate,

        RaffleStatus status,

        List<String> imageKeys,

        BigDecimal ticketPrice,

        Long firstTicketNumber,

        Long availableTickets,

        Long totalTickets,

        Long soldTickets,

        BigDecimal revenue,

        AssociationDTO association
) { }
