package com.raffleease.raffleease.Domains.Carts.DTO;

import com.raffleease.raffleease.Domains.Carts.Model.CartStatus;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CartEdit(
        CartStatus status,

        List<Ticket> tickets,

        LocalDateTime lastUpdated,

        String token
) {
}
