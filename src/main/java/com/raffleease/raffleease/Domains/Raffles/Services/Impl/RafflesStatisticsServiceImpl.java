package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
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
        raffle.setSoldTickets(raffle.getSoldTickets() + soldTickets);
        raffle.setClosedSells(raffle.getClosedSells() + 1);
        BigDecimal sellAmount = raffle.getTicketPrice().multiply(BigDecimal.valueOf(soldTickets));
        raffle.setRevenue(raffle.getRevenue().add(sellAmount));
        rafflesPersistence.save(raffle);
    }

    @Override
    public void setCancelStatistics(Raffle raffle, long cancelledTickets) {
        raffle.setFailedSells(raffle.getFailedSells() + 1);
        rafflesPersistence.save(raffle);
    }

    @Override
    public void setRefundStatistics(Raffle raffle, long refundTickets) {
        raffle.setRefundTickets(raffle.getRefundTickets() + refundTickets);
        raffle.setSoldTickets(raffle.getSoldTickets() - refundTickets);
        raffle.setClosedSells(raffle.getClosedSells() - 1);
        raffle.setFailedSells(raffle.getFailedSells() + 1);
        BigDecimal refundAmount = calculateAmount(raffle.getTicketPrice(), refundTickets);
        raffle.setRevenue(raffle.getRevenue().subtract(refundAmount));
        rafflesPersistence.save(raffle);
    }

    @Override
    public void setUnpaidStatistics(Raffle raffle, long unpaidTickets) {
        raffle.setUnpaidTickets(raffle.getUnpaidTickets() + unpaidTickets);
        raffle.setFailedSells(raffle.getFailedSells() + 1);
        rafflesPersistence.save(raffle);
    }

    private BigDecimal calculateAmount(BigDecimal ticketPrice, long numTickets) {
        return ticketPrice.multiply(BigDecimal.valueOf(numTickets));
    }
}
