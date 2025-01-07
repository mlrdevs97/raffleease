package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleEdit;

import java.util.List;

public interface IRafflesOrchestrator {
    void delete(Long id);

    PublicRaffleDTO publish(Long id);

    PublicRaffleDTO pause(Long id);

    PublicRaffleDTO restart(Long id);

    PublicRaffleDTO get(Long id);

    List<PublicRaffleDTO> getAll(String token);

    PublicRaffleDTO createRaffle(String token, RaffleCreate request);

    PublicRaffleDTO edit(Long id, RaffleEdit raffleEdit);
}
