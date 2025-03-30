package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface RaffleCreateService {
    PublicRaffleDTO create(HttpServletRequest request, RaffleCreate raffleData);
}
