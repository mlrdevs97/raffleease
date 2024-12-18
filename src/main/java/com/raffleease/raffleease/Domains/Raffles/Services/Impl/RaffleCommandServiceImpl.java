package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Repository.IRafflesRepository;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RaffleCommandServiceImpl {
    private final IRafflesRepository repository;

    public Raffle saveRaffle(Raffle raffle) {
        try {
            return repository.save(raffle);
        } catch (DataAccessException exp) {
            throw new DatabaseException("Database error occurred while saving raffle: " + exp.getMessage());
        }
    }
}