package com.raffleease.raffleease.Domains.Reservations.Services;

import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Reservations.DTOs.GenerateRandom;

public interface IReservationsRandomService {
    CartDTO generateRandom(GenerateRandom request, String cartId);
}
