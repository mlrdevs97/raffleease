package com.raffleease.raffleease.Domains.Tickets.DTO;

import com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;


@Builder
public record TicketDTO(
        @NotNull(message = "Ticket Id cannot be null")
        @Positive(message = "Ticket id must be a positive number")
        Long id,

        @NotBlank(message = "Ticket number cannot be blank")
        String ticketNumber,

        @NotNull(message = "Must indicate ticket status")
        TicketStatus status
) {
}
