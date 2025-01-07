package com.raffleease.raffleease.Domains.Raffles.DTOs;

import jakarta.validation.constraints.*;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record RaffleEdit(
        @Size(max = 100, message = "Tile cannot exceed 100 characters")
        String title,

        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        String description,

        @Future(message = "End date must be in the future")
        LocalDateTime endDate,

        @Size(max = 10, message = "A maximum of 10 pictures for raffle are allowed")
        List<Long> deleteImageIds,

        @NotNull(message = "Images for raffle are required")
        @Size(max = 10, message = "A maximum of 10 pictures for raffle are allowed")
        List<MultipartFile> newIMages,

        @DecimalMin(value = "0.0", inclusive = false, message = "Ticket price must be greater than 0")
        BigDecimal ticketPrice,

        @Positive(message = "Total tickets must be a positive number")
        Long totalTickets
) { }