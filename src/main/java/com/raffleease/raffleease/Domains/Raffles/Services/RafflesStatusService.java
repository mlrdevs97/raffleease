package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.StatusUpdate;

public interface RafflesStatusService {
    RaffleDTO updateStatus(Long id, StatusUpdate request);
}
