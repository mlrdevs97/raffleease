package com.raffleease.raffleease.Domains.Raffles.DTOs;

import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketsCreate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

import static com.raffleease.raffleease.Constants.Constants.MAX_IMAGES;
import static com.raffleease.raffleease.Constants.Constants.MIN_IMAGES;

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

        @NotNull(message = "Must provide at least one picture for raffle")
        @Size(min = MIN_IMAGES, max = MAX_IMAGES, message = "A minimum of 1 and a maximum of 10 images are allowed")
        List<ImageDTO> images,

        @NotNull(message = "Raffle ticket's info is required")
        @Valid
        TicketsCreate ticketsInfo
) {}
