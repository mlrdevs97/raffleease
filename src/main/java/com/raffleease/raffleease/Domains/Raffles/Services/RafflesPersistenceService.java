package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;

public interface RafflesPersistenceService {
    Raffle save(Raffle raffle);

    void delete(Raffle entity);

    void deleteById(Long id);

    Raffle findById(Long id);
}
