package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.StatusUpdate;
import com.raffleease.raffleease.Domains.Raffles.Mappers.IRafflesMapper;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesPersistenceService;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesStatusService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus.*;

@RequiredArgsConstructor
@Service
public class RafflesStatusServiceImpl implements RafflesStatusService {
    private final RafflesPersistenceService rafflesPersistence;
    private final IRafflesMapper mapper;

    public PublicRaffleDTO updateStatus(Long id, StatusUpdate request) {
        Raffle raffle = rafflesPersistence.findById(id);
        RaffleStatus newStatus = request.status();

        switch (newStatus) {
            case ACTIVE -> updateToActive(raffle);
            case PAUSED -> pause(raffle);
            case PENDING -> throw new BusinessException("Cannot revert to 'PENDING' state.");
            default -> throw new BusinessException("Unsupported status transition.");
        }
        return saveAndMap(raffle);
    }

    private void updateToActive(Raffle raffle) {
        if (raffle.getStatus() == PENDING) {
            raffle.setStatus(ACTIVE);
            raffle.setStartDate(LocalDateTime.now());
        } else if (raffle.getStatus() == PAUSED) {
            raffle.setStatus(ACTIVE);
        } else {
            throw new BusinessException("Invalid status transition to ACTIVE");
        }
    }

    private void pause(Raffle raffle) {
        if (!raffle.getStatus().equals(ACTIVE)) {
            throw new BusinessException("Only raffles in 'ACTIVE' state can be paused.");
        }
        raffle.setStatus(PAUSED);
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