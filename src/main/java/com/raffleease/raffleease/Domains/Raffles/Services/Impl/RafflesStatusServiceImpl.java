package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Mappers.RafflesMapper;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesCommandService;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesQueryService;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesStatusService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus.ACTIVE;
import static com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus.PAUSED;

@RequiredArgsConstructor
@Service
public class RafflesStatusServiceImpl implements IRafflesStatusService {
    private final IRafflesQueryService rafflesQueryService;
    private final IRafflesCommandService commandService;
    private final RafflesMapper mapper;

    public RaffleDTO publish(Long id) {
        Raffle raffle = rafflesQueryService.findById(id);
        if (!raffle.getStatus().equals(RaffleStatus.PENDING)) {
            throw new BusinessException("Only raffles in 'PENDING' state can be published.");
        }
        raffle.setStatus(ACTIVE);
        raffle.setStartDate(LocalDateTime.now());
        return saveAndMap(raffle);
    }

    public RaffleDTO pause(Long id) {
        Raffle raffle = rafflesQueryService.findById(id);
        if (!raffle.getStatus().equals(ACTIVE)) {
            throw new BusinessException("Only raffles in 'ACTIVE' state can be paused.");
        }
        raffle.setStatus(PAUSED);
        return saveAndMap(raffle);
    }

    public RaffleDTO restart(Long id) {
        Raffle raffle = rafflesQueryService.findById(id);
        if (!raffle.getStatus().equals(PAUSED)) {
            throw new BusinessException("Only raffles in 'PAUSED' state can be restarted.");
        }
        raffle.setStatus(ACTIVE);
        return saveAndMap(raffle);
    }

    public void delete(Long id) {
        Raffle raffle = rafflesQueryService.findById(id);
        if (!raffle.getStatus().equals(RaffleStatus.PENDING)) {
            throw new BusinessException("Only raffles in 'PENDING' state can be deleted.");
        }
    }

    private RaffleDTO saveAndMap(Raffle raffle) {
        Raffle savedRaffle = commandService.saveRaffle(raffle);
        return mapper.fromRaffle(savedRaffle);
    }
}
