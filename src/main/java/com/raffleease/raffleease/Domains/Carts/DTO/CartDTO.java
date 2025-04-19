package com.raffleease.raffleease.Domains.Carts.DTO;

import com.raffleease.raffleease.Domains.Carts.Model.CartOwnerType;
import com.raffleease.raffleease.Domains.Carts.Model.CartStatus;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketDTO;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CartDTO(
        Long id,
        Long customerId,
        List<TicketDTO> tickets,
        CartStatus status,
        CartOwnerType ownerType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
