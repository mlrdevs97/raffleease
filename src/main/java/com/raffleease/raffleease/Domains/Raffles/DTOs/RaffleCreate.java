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

import static com.raffleease.raffleease.Common.Constants.Constants.MAX_IMAGES;
import static com.raffleease.raffleease.Common.Constants.Constants.MIN_IMAGES;

public record RaffleCreate(
        @NotBlank
        @Size(max = 100)
        String title,

        @NotBlank
        @Size(max = 5000)
        String description,

        @NotNull
        @Future
        LocalDateTime endDate,

        @NotNull
        @Size(min = MIN_IMAGES, max = MAX_IMAGES)
        List<ImageDTO> images,

        @NotNull
        @Valid
        TicketsCreate ticketsInfo
) {}