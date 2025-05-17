package com.raffleease.raffleease.Domains.Raffles.DTOs;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Raffles.Model.CompletionReason;
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
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt,
        RaffleStatus status,
        List<ImageDTO> images,
        BigDecimal ticketPrice,
        Long firstTicketNumber,
        Long availableTickets,
        Long totalTickets,
        Long soldTickets,
        BigDecimal revenue,
        CompletionReason completionReason,
        Long winningTicketId
) { }