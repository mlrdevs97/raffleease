package com.raffleease.raffleease.Domains.Tickets.Repository;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface ICustomTicketsRepository {
    void updateStatusAndReservationFlag(LocalDateTime reservationTime);
    void updateReservationTime(LocalDateTime reservationTime);
    void setRaffle(Raffle raffle, Set<Ticket> tickets);
    List<Object[]> findRafflesAndUpdatedTicketCount(LocalDateTime threshold);
}
