package com.raffleease.raffleease.Domains.Tickets.DTO;

import com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TicketDTO(
        @NotNull(message = "Ticket Id cannot be null")
        @Positive(message = "Ticket id must be a positive number")
        Long id,

        @NotNull(message = "Raffle Id cannot be null")
        @Positive(message = "Raffle id must be a positive number")
        Long raffleId,

        @NotBlank(message = "Ticket number cannot be blank")
        String ticketNumber,

        @NotNull(message = "Ticket price is required")
        @Positive(message = "Ticket price must positive")
        BigDecimal price,

        @NotNull(message = "Must indicate ticket status")
        TicketStatus status,

        String reservationFlag,

        LocalDateTime reservationTime,

        String customerId
) {
}
