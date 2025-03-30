package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleEdit;
import com.raffleease.raffleease.Domains.Raffles.DTOs.StatusUpdate;
import com.raffleease.raffleease.Domains.Raffles.Services.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RafflesOrchestratorImpl implements RafflesOrchestrator {
    private final RafflesQueryService queryService;
    private final RaffleCreateService createService;
    private final RafflesStatusService rafflesStatusService;
    private final RafflesPersistenceService rafflesPersistence;
    private final RafflesEditService rafflesEditService;

    @Override
    public void delete(Long id) { rafflesPersistence.deleteById(id); }

    @Override
    public PublicRaffleDTO updateStatus(Long id, StatusUpdate request) {
        return rafflesStatusService.updateStatus(id, request);
    }

    @Override
    public PublicRaffleDTO getAll(Long id) {
        return queryService.getAll(id);
    }

    @Override
    public List<PublicRaffleDTO> getAll(HttpServletRequest request) {
        return queryService.getAll(request);
    }

    @Override
    public PublicRaffleDTO create(HttpServletRequest request, RaffleCreate raffleData) {
        return createService.create(request, raffleData);
    }

    @Override
    public PublicRaffleDTO edit(Long id, RaffleEdit raffleEdit) {
        return rafflesEditService.updateStatistics(id, raffleEdit);
    }
}
