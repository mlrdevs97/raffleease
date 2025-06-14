package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;

public interface RafflesCreateService {
    RaffleDTO create(Long associationId, RaffleCreate raffleData);
}
