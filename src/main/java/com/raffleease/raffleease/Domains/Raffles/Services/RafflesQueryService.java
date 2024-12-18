package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Mappers.RafflesMapper;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Repository.IRafflesRepository;
import com.raffleease.raffleease.Domains.Raffles.Repository.ImagesRepository;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class RafflesQueryService {
    private final IRafflesRepository rafflesRepository;
    private final ImagesRepository imagesRepository;
    private final RafflesMapper mapper;

    public RaffleDTO get(Long id) {
        Raffle raffle = findById(id);
        return mapper.fromRaffle(raffle);
    }

    public Raffle findById(Long id) {
        return this.rafflesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Raffle with id <" + id + "> not found"));
    }

    public Set<RaffleDTO> getAll() {
        Set<Raffle> raffles = findByAssociation(association);
        return mapper.fromRaffleSet(raffles);
    }

    public Set<Raffle> findByAssociation(Long associationId) {
        try {
            return new HashSet<>(rafflesRepository.findByAssociation(associationId));
        } catch (Exception exp) {
            throw new DatabaseException("Failed to access database while retrieving raffles: " + exp.getMessage());
        }
    }
}
