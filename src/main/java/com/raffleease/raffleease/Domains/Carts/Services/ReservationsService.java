package com.raffleease.raffleease.Domains.Carts.Services;

import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.DTO.ReservationRequest;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;

import java.util.List;

public interface ReservationsService {
    CartDTO reserve(ReservationRequest request, Long associationId, Long cartId);
    void release(ReservationRequest request, Long associationId, Long cartId);
}
