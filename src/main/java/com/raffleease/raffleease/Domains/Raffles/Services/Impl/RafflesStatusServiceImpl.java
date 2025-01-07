package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Mappers.IRafflesMapper;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesPersistenceService;
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
    private final IRafflesPersistenceService rafflesPersistence;
    private final IRafflesMapper mapper;

    public PublicRaffleDTO publish(Long id) {
        Raffle raffle = rafflesPersistence.findById(id);
        if (!raffle.getStatus().equals(RaffleStatus.PENDING)) {
            throw new BusinessException("Only raffles in 'PENDING' state can be published.");
        }
        raffle.setStatus(ACTIVE);
        raffle.setStartDate(LocalDateTime.now());
        return saveAndMap(raffle);
    }

    public PublicRaffleDTO pause(Long id) {
        Raffle raffle = rafflesPersistence.findById(id);
        if (!raffle.getStatus().equals(ACTIVE)) {
            throw new BusinessException("Only raffles in 'ACTIVE' state can be paused.");
        }
        raffle.setStatus(PAUSED);
        return saveAndMap(raffle);
    }

    public PublicRaffleDTO restart(Long id) {
        Raffle raffle = rafflesPersistence.findById(id);
        if (!raffle.getStatus().equals(PAUSED)) {
            throw new BusinessException("Only raffles in 'PAUSED' state can be restarted.");
        }
        raffle.setStatus(ACTIVE);
        return saveAndMap(raffle);
    }

    public void delete(Long id) {
        Raffle raffle = rafflesPersistence.findById(id);
        if (!raffle.getStatus().equals(RaffleStatus.PENDING)) {
            throw new BusinessException("Only raffles in 'PENDING' state can be deleted.");
        }
    }

    private PublicRaffleDTO saveAndMap(Raffle raffle) {
        return mapper.fromRaffle(rafflesPersistence.save(raffle));
    }
}
