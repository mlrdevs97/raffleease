package com.raffleease.raffleease.Domains.Carts.DTO;

import com.raffleease.raffleease.Domains.Carts.Model.CartStatus;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CartDTO(
        @NotNull(message = "Cart Id cannot be null")
        @Positive(message = "Cart id must be a positive number")
        Long cartId,

        @NotNull(message = "Raffle Id cannot be null")
        @Positive(message = "Raffle id must be a positive number")
        Long raffleId,

        List<TicketDTO> tickets,

        CartStatus status,

        LocalDateTime lastModified
) { }
