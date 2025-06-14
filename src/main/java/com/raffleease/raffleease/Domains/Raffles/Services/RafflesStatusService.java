package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.StatusUpdate;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;

public interface RafflesStatusService {
    RaffleDTO updateStatus(Long id, StatusUpdate request);
    void delete(Long id);
    void completeRaffleIfAllTicketsSold(Raffle raffle);
    void updateStatusAfterAvailableTicketsIncrease(Raffle raffle);
}
