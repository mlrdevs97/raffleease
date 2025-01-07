package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;

import java.util.List;

public interface IRafflesQueryService {
    PublicRaffleDTO get(Long id);
    List<PublicRaffleDTO> getAll(String token);
}
