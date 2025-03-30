package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.StatusUpdate;

public interface RafflesStatusService {
    PublicRaffleDTO updateStatus(Long id, StatusUpdate request);
    void delete(Long id);
}
