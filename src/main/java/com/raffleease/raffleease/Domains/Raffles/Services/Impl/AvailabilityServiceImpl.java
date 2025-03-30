package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.AvailabilityService;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesPersistenceService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AvailabilityServiceImpl implements AvailabilityService {
    private final RafflesPersistenceService rafflesPersistence;

    @Override
    public void reduceAvailableTickets(Long raffleId, long reductionQuantity) {
        Raffle raffle = rafflesPersistence.findById(raffleId);
        long availableTickets = raffle.getAvailableTickets() - reductionQuantity;
        if (availableTickets < 0) {
            throw new BusinessException("Insufficient tickets available to complete the operation");
        }
        raffle.setAvailableTickets(availableTickets);
        rafflesPersistence.save(raffle);
    }

    @Override
    public void increaseAvailableTickets(Raffle raffle, long increaseQuantity) {
        Long availableTickets = raffle.getAvailableTickets() + increaseQuantity;
        if (availableTickets > raffle.getTotalTickets()) {
            throw new BusinessException("The operation exceeds the total ticket limit");
        }
        raffle.setAvailableTickets(availableTickets);
        rafflesPersistence.save(raffle);
    }
}
