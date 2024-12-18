package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleEdit;
import com.raffleease.raffleease.Domains.Raffles.Services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class RafflesOrchestratorImpl implements IRafflesOrchestrator {
    private final IRaffleCreateService createService;
    private final IRafflesQueryService rafflesQueryService;
    private final IRafflesStatusService rafflesStatusService;
    private final IRaffleDeleteService raffleDeleteService;
    private final IRafflesEditService rafflesEditService;

    @Override
    public void delete(Long id) {
        raffleDeleteService.delete(id);
    }

    @Override
    public RaffleDTO publish(Long id) {
        return rafflesStatusService.publish(id);
    }

    @Override
    public RaffleDTO pause(Long id) {
        return rafflesStatusService.pause(id);
    }

    @Override
    public RaffleDTO restart(Long id) {
        return rafflesStatusService.restart(id);
    }

    @Override
    public RaffleDTO get(Long id) { return rafflesQueryService.get(id); }

    @Override
    public Set<RaffleDTO> getAll() {
        return rafflesQueryService.getAll();
    }

    @Override
    public RaffleDTO createRaffle(RaffleCreate request) {
        return createService.createRaffle(request);
    }

    public RaffleDTO edit(Long id, RaffleEdit editRaffle) {
        return rafflesEditService.edit(id, editRaffle);
    }
}
