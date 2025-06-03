package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatistics;
import com.raffleease.raffleease.Domains.Raffles.Services.RaffleTicketsAvailabilityService;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesPersistenceService;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RaffleTicketsAvailabilityServiceImpl implements RaffleTicketsAvailabilityService {
    private final RafflesPersistenceService rafflesPersistence;

    @Override
    public void reduceAvailableTickets(Raffle raffle, long reductionQuantity) {
        RaffleStatistics statistics = raffle.getStatistics();
        long availableTickets = statistics.getAvailableTickets() - reductionQuantity;
        if (availableTickets < 0) {
            throw new BusinessException("Insufficient tickets available to complete the operation");
        }
        statistics.setAvailableTickets(availableTickets);
        rafflesPersistence.save(raffle);
    }

    @Override
    public void increaseAvailableTickets(Raffle raffle, long increaseQuantity) {
        RaffleStatistics statistics = raffle.getStatistics();
        Long availableTickets = statistics.getAvailableTickets() + increaseQuantity;
        if (availableTickets > raffle.getTotalTickets()) {
            throw new BusinessException("The operation exceeds the total ticket limit");
        }
        statistics.setAvailableTickets(availableTickets);
        rafflesPersistence.save(raffle);
    }
}
