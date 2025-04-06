package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;

import java.util.List;

public interface RafflesPersistenceService {
    Raffle save(Raffle raffle);

    void delete(Raffle entity);

    Raffle findById(Long id);

    void saveAll(List<Raffle> raffles);
}
