package com.raffleease.raffleease.Domains.Carts.Services;

import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Carts.DTO.ReservationRequest;

public interface ReservationsService {
    /**
     * Reserves specific tickets from a cart.
     * 
     * @param request The request containing ticket IDs to reserve
     * @param associationId The ID of the association
     * @param cartId The ID of the cart
     * @return The updated cart DTO
     */
    CartDTO reserve(ReservationRequest request, Long associationId, Long cartId);

    /**
     * Releases specific tickets from a cart (partial release).
     * 
     * @param request The request containing ticket IDs to release
     * @param associationId The ID of the association
     * @param cartId The ID of the cart
     */
    void release(ReservationRequest request, Long associationId, Long cartId);
}
