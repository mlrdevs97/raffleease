package com.raffleease.raffleease.Domains.Reservations.Services;

import com.raffleease.raffleease.Domains.Cart.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Reservations.DTOs.ReservationRequest;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;

import java.util.List;

public interface IReservationsCreateService {
    CartDTO reserve(ReservationRequest request, String cartId);
    CartDTO reserve(Long raffleId, List<Ticket> tickets, String cartId);
}
