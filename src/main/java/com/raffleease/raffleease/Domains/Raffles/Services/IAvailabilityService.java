package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;

public interface IAvailabilityService {
    void reduceAvailableTickets(Long raffleId, long reductionQuantity);
    void increaseAvailableTickets(Long raffleId, long increaseQuantity);
}