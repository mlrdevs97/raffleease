package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.StatusUpdate;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;

public interface RafflesStatusService {
    RaffleDTO updateStatus(Raffle raffle, RaffleStatus newStatus);
    RaffleDTO updateStatus(Long id, StatusUpdate request);
}
