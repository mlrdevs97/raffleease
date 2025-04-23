package com.raffleease.raffleease.Domains.Raffles.DTOs;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Raffles.Model.CompletionReason;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PublicRaffleDTO(
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
        AssociationDTO association,
        CompletionReason completionReason,
        Long winningTicketId
) { }