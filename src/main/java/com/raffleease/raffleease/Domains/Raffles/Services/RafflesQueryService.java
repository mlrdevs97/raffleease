package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface RafflesQueryService {
    PublicRaffleDTO getAll(Long id);
    List<PublicRaffleDTO> getAll(HttpServletRequest request);
}
