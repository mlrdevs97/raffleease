package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleEdit;
import com.raffleease.raffleease.Domains.Raffles.Services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RafflesOrchestratorImpl implements IRafflesOrchestrator {
    private final IRafflesQueryService queryService;
    private final IRaffleCreateService createService;
    private final IRafflesStatusService rafflesStatusService;
    private final IRafflesPersistenceService rafflesPersistence;
    private final IRafflesEditService rafflesEditService;

    @Override
    public void delete(Long id) { rafflesPersistence.deleteById(id); }

    @Override
    public PublicRaffleDTO publish(Long id) {
        return rafflesStatusService.publish(id);
    }

    @Override
    public PublicRaffleDTO pause(Long id) {
        return rafflesStatusService.pause(id);
    }

    @Override
    public PublicRaffleDTO restart(Long id) {
        return rafflesStatusService.restart(id);
    }

    @Override
    public PublicRaffleDTO get(Long id) {
        return queryService.get(id);
    }

    @Override
    public List<PublicRaffleDTO> getAll(String token) {
        return queryService.getAll(token);
    }

    @Override
    public PublicRaffleDTO createRaffle(String token, RaffleCreate request) {
        return createService.createRaffle(token, request);
    }

    public PublicRaffleDTO edit(Long id, RaffleEdit raffleEdit) {
        return rafflesEditService.edit(id, raffleEdit);
    }
}
