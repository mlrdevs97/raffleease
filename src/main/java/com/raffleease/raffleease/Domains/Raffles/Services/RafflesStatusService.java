package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.StatusUpdate;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;

public interface RafflesStatusService {
    PublicRaffleDTO updateStatus(Raffle raffle, RaffleStatus newStatus);
    PublicRaffleDTO updateStatus(Long id, StatusUpdate request);
}
