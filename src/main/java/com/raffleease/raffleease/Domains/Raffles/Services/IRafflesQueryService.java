package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;

import java.util.List;

public interface IRafflesQueryService {
    RaffleDTO get(Long id);
    Raffle findById(Long id);
    List<RaffleDTO> getAll();
}
