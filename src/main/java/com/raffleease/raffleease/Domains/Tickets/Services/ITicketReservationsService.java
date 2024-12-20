package com.raffleease.raffleease.Domains.Tickets.Services;

import com.raffleease.raffleease.Domains.Orders.DTOs.Reservation;
import com.raffleease.raffleease.Domains.Tickets.DTO.ReservationRequest;
import com.raffleease.raffleease.Domains.Tickets.DTO.ReservationResponse;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import java.util.Set;

public interface ITicketReservationsService {
    void release(ReservationRequest request);

    ReservationResponse reserve(ReservationRequest request);

    ReservationResponse reserve(Long raffleId, Set<Ticket> tickets);

    Boolean checkReservation(Set<Reservation> reservations);

    void releaseScheduled();
}
