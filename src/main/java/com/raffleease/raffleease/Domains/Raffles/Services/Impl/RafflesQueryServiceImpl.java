package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Mappers.RafflesMapper;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Repository.IRafflesRepository;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesQueryService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RafflesQueryServiceImpl implements IRafflesQueryService {
    private final IRafflesRepository rafflesRepository;
    private final RafflesMapper mapper;

    public RaffleDTO get(Long id) {
        Raffle raffle = findById(id);
        return mapper.fromRaffle(raffle);
    }

    public Raffle findById(Long id) {
        return this.rafflesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Raffle with id <" + id + "> not found"));
    }

    public List<RaffleDTO> getAll() {
        // TODO
        Association association = new Association();
        List<Raffle> raffles = findByAssociation(association);
        return mapper.fromRaffleList(raffles);
    }

    private List<Raffle> findByAssociation(Association association) {
        try {
            return rafflesRepository.findByAssociation(association);
        } catch (DataAccessException exp) {
            throw new DatabaseException("Database error occurred while retrieving association: " + exp.getMessage());
        }
    }
}
