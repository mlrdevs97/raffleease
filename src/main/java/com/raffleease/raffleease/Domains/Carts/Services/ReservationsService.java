package com.raffleease.raffleease.Domains.Carts.Services;

import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.DTO.ReservationRequest;

public interface ReservationsService {
    CartDTO reserve(ReservationRequest request, Long associationId, Long cartId);
    CartDTO reserve(ReservationRequest request, Long cartId);
    void release(Cart cart);
    void release(ReservationRequest request, Long cartId);
    void release(ReservationRequest request, Long associationId, Long cartId);
}
