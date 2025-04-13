package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;

public interface RaffleTicketsAvailabilityService {
    void reduceAvailableTickets(Raffle raffle, long reductionQuantity);
    void increaseAvailableTickets(Raffle raffle, long increaseQuantity);
}