package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleEdit;

import java.util.List;

public interface IRafflesOrchestrator {
    void delete(Long id);

    RaffleDTO publish(Long id);

    RaffleDTO pause(Long id);

    RaffleDTO restart(Long id);

    RaffleDTO get(Long id);

    List<RaffleDTO> getAll();

    RaffleDTO createRaffle(RaffleCreate request);

    RaffleDTO edit(Long id, RaffleEdit editRaffle);
}
