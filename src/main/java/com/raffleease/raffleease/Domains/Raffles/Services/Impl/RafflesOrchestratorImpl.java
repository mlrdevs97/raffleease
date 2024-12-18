package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.*;
import com.raffleease.raffleease.Domains.Raffles.Services.Impl.RafflesStatusServiceImpl;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Page;
import org.springframework.stereotype.Service;

import java.util.Set;


@RequiredArgsConstructor
@Service
public class RafflesOrchestratorImpl implements IRafflesOrchestrator {
    private final IRaffleCreateService createService;
    private final IRafflesQueryService rafflesQueryService;
    private final RafflesStatusServiceImpl rafflesStatusServiceImpl;
    private final IRaffleDeleteService raffleDeleteService;
    private final IRafflesEditService rafflesEditService;

    public void delete(long id) {
        raffleDeleteService.delete(id);
    }

    public RaffleDTO publish(Long id) {
        return rafflesStatusServiceImpl.publish(id);
    }

    public RaffleDTO pause(Long id) {
        return rafflesStatusServiceImpl.pause(id);
    }

    public RaffleDTO restart(Long id) {
        return rafflesStatusServiceImpl.restart(id);
    }

    public Set<RaffleDTO> getAll() {
        return rafflesQueryService.getAll();
    }

    public RaffleDTO createRaffle(RaffleCreate request) {
        return createService.createRaffle(request);
    }
}
