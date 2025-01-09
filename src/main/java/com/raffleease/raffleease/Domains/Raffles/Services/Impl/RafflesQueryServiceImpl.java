package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.IAssociationsService;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Mappers.IRafflesMapper;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Repository.IRafflesRepository;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesPersistenceService;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesQueryService;
import com.raffleease.raffleease.Domains.Token.Services.ITokensQueryService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RafflesQueryServiceImpl implements IRafflesQueryService {
    private final IRafflesPersistenceService rafflesPersistence;
    private final IRafflesRepository rafflesRepository;
    private final IRafflesMapper mapper;
    private final ITokensQueryService tokensQueryService;
    private final IAssociationsService associationsService;

    public PublicRaffleDTO get(Long id) {
        return mapper.fromRaffle(rafflesPersistence.findById(id));
    }

    public List<PublicRaffleDTO> getAll(String token) {
        String subject = tokensQueryService.getSubject(token);
        Long id = Long.parseLong(subject);
        Association association = associationsService.findById(id);
        return mapper.fromRaffleList(findByAssociation(association));
    }

    private List<Raffle> findByAssociation(Association association) {
        try {
            return rafflesRepository.findByAssociation(association);
        } catch (DataAccessException exp) {
            throw new DatabaseException("Database error occurred while retrieving association: " + exp.getMessage());
        }
    }
}
