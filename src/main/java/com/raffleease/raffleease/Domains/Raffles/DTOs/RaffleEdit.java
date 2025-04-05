package com.raffleease.raffleease.Domains.Raffles.DTOs;

import com.raffleease.raffleease.Constants.ImagesConstraints;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.raffleease.raffleease.Constants.ImagesConstraints.MAX_IMAGES;
import static com.raffleease.raffleease.Constants.ImagesConstraints.MIN_IMAGES;

@Builder
public record RaffleEdit(
        @Size(max = 100, message = "Tile cannot exceed 100 characters")
        String title,

        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        String description,

        @Future(message = "End date must be in the future")
        LocalDateTime endDate,

        @Size(min = MIN_IMAGES, max = MAX_IMAGES, message = "A minimum of 1 and a maximum of 10 images are allowed")
        List<ImageDTO> images,

        @DecimalMin(value = "0.0", inclusive = false, message = "Ticket price must be greater than 0")
        BigDecimal ticketPrice,

        @Positive(message = "Total tickets must be a positive number")
        Long totalTickets,

        @Positive(message = "Price must be greater than zero")
        BigDecimal price
) { }