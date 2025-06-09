package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;

import java.util.List;

public interface RafflesStatisticsService {
    void setReservationStatistics(Raffle raffle, long reductionQuantity);
    void setReleaseStatistics(Raffle raffle, long increaseQuantity);
    void setCreateOrderStatistics(Raffle raffle, long reservedTickets);
    void setCompleteStatistics(Raffle raffle, long soldTickets);
    void setRefundStatistics(Raffle raffle, long cancelTickets);
    void setCancelStatistics(Raffle raffle, long cancelledTickets);
    void setUnpaidStatistics(Raffle raffle, long unpaidTickets);
    void increaseRafflesTicketsAvailability(List<Ticket> tickets);
    void reduceRaffleTicketsAvailability(List<Ticket> tickets);
}
