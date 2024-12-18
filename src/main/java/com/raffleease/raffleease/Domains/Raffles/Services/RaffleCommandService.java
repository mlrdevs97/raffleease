package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Repository.IRafflesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RaffleCommandService {
    private final IRafflesRepository repository;

    public Raffle saveRaffle(Raffle raffle) {
        try {
            return repository.save(raffle);
        } catch (Exception exp) {
            throw new DatabaseException("Error accessing database while saving raffle: " + exp.getMessage());
        }
    }
}
