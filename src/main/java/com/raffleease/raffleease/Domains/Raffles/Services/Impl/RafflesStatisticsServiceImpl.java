package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatistics;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesStatisticsService;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class RafflesStatisticsServiceImpl implements RafflesStatisticsService {
    private final RafflesPersistenceService rafflesPersistence;

    @Override
    public void setSellStatistics(Raffle raffle, long soldTickets) {
        RaffleStatistics statistics = raffle.getStatistics();
        statistics.setSoldTickets(statistics.getSoldTickets() + soldTickets);
        statistics.setClosedSells(statistics.getClosedSells() + 1);
        BigDecimal sellAmount = raffle.getTicketPrice().multiply(BigDecimal.valueOf(soldTickets));
        statistics.setRevenue(statistics.getRevenue().add(sellAmount));
        rafflesPersistence.save(raffle);
    }

    @Override
    public void setCancelStatistics(Raffle raffle, long cancelledTickets) {
        RaffleStatistics statistics = raffle.getStatistics();
        statistics.setFailedSells(statistics.getFailedSells() + 1);
        rafflesPersistence.save(raffle);
    }

    @Override
    public void setRefundStatistics(Raffle raffle, long refundTickets) {
        RaffleStatistics statistics = raffle.getStatistics();
        statistics.setRefundTickets(statistics.getRefundTickets() + refundTickets);
        statistics.setSoldTickets(statistics.getSoldTickets() - refundTickets);
        statistics.setClosedSells(statistics.getClosedSells() - 1);
        statistics.setFailedSells(statistics.getFailedSells() + 1);
        BigDecimal refundAmount = calculateAmount(raffle.getTicketPrice(), refundTickets);
        statistics.setRevenue(statistics.getRevenue().subtract(refundAmount));
        rafflesPersistence.save(raffle);
    }

    @Override
    public void setUnpaidStatistics(Raffle raffle, long unpaidTickets) {
        RaffleStatistics statistics = raffle.getStatistics();
        statistics.setUnpaidTickets(statistics.getUnpaidTickets() + unpaidTickets);
        statistics.setFailedSells(statistics.getFailedSells() + 1);
        rafflesPersistence.save(raffle);
    }

    private BigDecimal calculateAmount(BigDecimal ticketPrice, long numTickets) {
        return ticketPrice.multiply(BigDecimal.valueOf(numTickets));
    }
}
