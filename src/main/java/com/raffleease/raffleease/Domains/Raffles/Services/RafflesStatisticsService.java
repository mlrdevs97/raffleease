package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;

public interface RafflesStatisticsService {
    void setSellStatistics(Raffle raffle, long soldTickets);
    void setRefundStatistics(Raffle raffle, long cancelTickets);
    void setCancelStatistics(Raffle raffle, long cancelledTickets);
    void setUnpaidStatistics(Raffle raffle, long unpaidTickets);
}
