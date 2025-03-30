package com.raffleease.raffleease.Domains.Reservations.Services;

import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Reservations.DTOs.ReleaseRequest;

public interface IReservationsReleaseService {
    void release(Cart cart);
    void release(ReleaseRequest request, Long cartId);
}
