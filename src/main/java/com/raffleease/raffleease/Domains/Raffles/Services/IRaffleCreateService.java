package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;

public interface IRaffleCreateService {
    PublicRaffleDTO createRaffle(String authHeader, RaffleCreate request);
}
