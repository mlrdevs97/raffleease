package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleEdit;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;

import java.math.BigDecimal;

public interface RafflesEditService {
    RaffleDTO edit(Long id, RaffleEdit raffleEdit);
}
