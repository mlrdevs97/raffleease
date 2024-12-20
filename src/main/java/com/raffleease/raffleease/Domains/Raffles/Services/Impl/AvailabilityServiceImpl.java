package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.IAvailabilityService;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesCommandService;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesQueryService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AvailabilityServiceImpl implements IAvailabilityService {
    private final IRafflesQueryService rafflesQueryService;
    private final IRafflesCommandService raffleCommandService;

    @Override
    public void reduceAvailableTickets(Long raffleId, long reductionQuantity) {
        Raffle raffle = rafflesQueryService.findById(raffleId);
        long availableTickets = raffle.getAvailableTickets() - reductionQuantity;
        if (availableTickets < 0) {
            throw new BusinessException("Insufficient tickets available to complete the operation");
        }
        raffle.setAvailableTickets(availableTickets);
        raffleCommandService.saveRaffle(raffle);
    }

    @Override
    public void increaseAvailableTickets(Long raffleId, long increaseQuantity) {
        Raffle raffle = rafflesQueryService.findById(raffleId);
        Long availableTickets = raffle.getAvailableTickets() + increaseQuantity;
        if (availableTickets > raffle.getTotalTickets()) {
            throw new BusinessException("The operation exceeds the total ticket limit");
        }
        raffle.setAvailableTickets(availableTickets);
        raffleCommandService.saveRaffle(raffle);
    }
}
