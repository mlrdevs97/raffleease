package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleEdit;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;

import java.math.BigDecimal;

public interface RafflesEditService {
    PublicRaffleDTO edit(Long id, RaffleEdit raffleEdit);
    void updateStatistics(Raffle raffle, BigDecimal revenue, Long soldTickets);
}
