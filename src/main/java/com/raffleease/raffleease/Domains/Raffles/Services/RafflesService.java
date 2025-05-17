package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;

public interface RafflesService {
    RaffleDTO create(Long associationId, RaffleCreate raffleData);
    void delete(Long id);
}
