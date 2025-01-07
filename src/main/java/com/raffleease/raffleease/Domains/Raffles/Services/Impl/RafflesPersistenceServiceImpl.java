package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Repository.IRafflesRepository;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesPersistenceService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.ConflictException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RafflesPersistenceServiceImpl implements IRafflesPersistenceService {
    private final IRafflesRepository repository;

    @Override
    public Raffle findById(Long id) {
        try {
            return repository.findById(id).orElseThrow(() -> new NotFoundException("Raffle not found for id <" + id + ">"));
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while fetching raffle with ID <" + id + ">: " + ex.getMessage());
        }
    }

    @Override
    public Raffle save(Raffle raffles) {
        try {
            return repository.save(raffles);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Failed to save raffle due to unique constraint violation: " + ex.getMessage());
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving raffle: " + ex.getMessage());
        }
    }

    @Override
    public void delete(Raffle raffle) {
        try {
            repository.delete(raffle);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while deleting raffle: " + ex.getMessage());
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            repository.deleteById(id);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while deleting raffle with id <" + id + ">: " + ex.getMessage());
        }
    }
}
