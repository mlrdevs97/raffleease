package com.raffleease.raffleease.Domains.Raffles.DTOs;

import com.raffleease.raffleease.Domains.Tickets.DTO.TicketsCreate;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public record RaffleCreate(
        @NotBlank(message = "Raffle title is required")
        @Size(max = 100, message = "Tile cannot exceed 100 characters")
        String title,

        @NotBlank(message = "Raffle's description is required")
        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        String description,

        @NotNull(message = "Raffle's end date is required")
        @Future(message = "Raffle's end date must be in the future")
        LocalDateTime endDate,

        @NotNull(message = "Raffle ticket's info is required")
        @Validated
        TicketsCreate ticketsInfo
) { }
