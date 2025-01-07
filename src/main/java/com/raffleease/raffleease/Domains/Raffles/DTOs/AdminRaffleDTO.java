package com.raffleease.raffleease.Domains.Raffles.DTOs;

import java.math.BigDecimal;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record AdminRaffleDTO(
        @NotNull(message = "The raffle ID must not be null.")
        Long id,

        @NotNull(message = "The title must not be null.")
        @Size(min = 1, max = 255, message = "The title must be between 1 and 255 characters.")
        String title,

        @Size(max = 5000, message = "The description must not exceed 5000 characters.")
        String description,

        @Size(max = 2083, message = "The URL must not exceed 2083 characters.")
        String url,

        LocalDateTime startDate,

        LocalDateTime endDate,

        @NotNull(message = "The raffle status must not be null.")
        RaffleStatus status,

        @NotNull(message = "The list of image keys must not be null.")
        List<String> imageKeys,

        @NotNull(message = "The ticket price must not be null.")
        @Min(value = 0, message = "The ticket price must be at least 0.")
        BigDecimal ticketPrice,

        @Min(value = 0, message = "The first ticket number must be at least 0.")
        Long firstTicketNumber,

        @Min(value = 0, message = "The number of available tickets must be at least 0.")
        Long availableTickets,

        @Min(value = 0, message = "The total number of tickets must be at least 0.")
        Long totalTickets,

        @NotNull(message = "The association must not be null.")
        AssociationDTO association,

        @NotNull(message = "The revenue must not be null.")
        @Min(value = 0, message = "The revenue must be at least 0.")
        BigDecimal revenue,

        @NotNull(message = "The number of sold tickets must not be null.")
        @Min(value = 0, message = "The number of sold tickets must be at least 0.")
        Long soldTickets
) {
}
