package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleEdit;
import com.raffleease.raffleease.Domains.Raffles.DTOs.StatusUpdate;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface RafflesOrchestrator {
    void delete(Long id);

    PublicRaffleDTO updateStatus(Long id, StatusUpdate request);

    PublicRaffleDTO getAll(Long id);

    List<PublicRaffleDTO> getAll(HttpServletRequest request);

    PublicRaffleDTO create(HttpServletRequest request, RaffleCreate raffleData);

    PublicRaffleDTO edit(Long id, RaffleEdit raffleEdit);
}
