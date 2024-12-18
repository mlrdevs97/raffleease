package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Services.IRaffleCreateService;
import com.raffleease.raffleease.Domains.Raffles.Services.IRaffleDeleteService;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesOrchestrator;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesQueryService;
import com.raffleease.raffleease.Domains.Raffles.Services.Impl.RafflesStatusServiceImpl;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Page;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class RafflesOrchestratorImpl implements IRafflesOrchestrator {
    private final IRaffleCreateService createService;
    private final IRafflesQueryService rafflesQueryService;
    private final RafflesStatusServiceImpl rafflesStatusServiceImpl;
    private final IRaffleDeleteService raffleDeleteService;
    private final IRaffleEditService rafflesEditService;

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

    public Page getAll() {
        return rafflesQueryService.getAll();
    }

    public RaffleDTO createRaffle(RaffleCreate request) {
        return createService.createRaffle(request);
    }
}
