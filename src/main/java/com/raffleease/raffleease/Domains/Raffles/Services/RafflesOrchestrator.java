package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Page;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class RafflesOrchestrator {
    private final RaffleCreateService createService;
    private final RafflesQueryService rafflesQueryService;
    private final RafflesStatusService rafflesStatusService;
    private final RaffleDeleteService raffleDeleteService;
    private final RafflesEditService rafflesEditService;

    public void delete(long id) {
        raffleDeleteService.delete(id);
    }

    public RaffleDTO publish(Long id) {
        return rafflesStatusService.publish(id);
    }

    public RaffleDTO pause(Long id) {
        return rafflesStatusService.pause(id);
    }

    public RaffleDTO restart(Long id) {
        return rafflesStatusService.restart(id);
    }

    public Page getAll() {
        return rafflesQueryService.getAll();
    }

    public RaffleDTO createRaffle(RaffleCreate request) {
        return createService.createRaffle(request);
    }
}
