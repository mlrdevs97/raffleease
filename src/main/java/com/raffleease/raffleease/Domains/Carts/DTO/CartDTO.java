package com.raffleease.raffleease.Domains.Carts.DTO;

import com.raffleease.raffleease.Domains.Carts.Model.CartOwnerType;
import com.raffleease.raffleease.Domains.Carts.Model.CartStatus;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketDTO;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CartDTO(
        @NotNull(message = "Cart Id cannot be null")
        @Positive(message = "Cart id must be a positive number")
        Long id,

        Long customerId,

        List<TicketDTO> tickets,

        CartStatus status,

        @Column(nullable = false)
        CartOwnerType ownerType,
        LocalDateTime createdAt,
        LocalDateTime lastModified
) { }
