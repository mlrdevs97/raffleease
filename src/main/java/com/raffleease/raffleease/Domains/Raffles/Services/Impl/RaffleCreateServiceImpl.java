package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.IAssociationsService;
import com.raffleease.raffleease.Domains.Images.Services.IImagesService;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Mappers.IRafflesMapper;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.IRaffleCreateService;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesPersistenceService;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsService;
import com.raffleease.raffleease.Domains.Token.Services.ITokensQueryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class RaffleCreateServiceImpl implements IRaffleCreateService {
    private final IRafflesPersistenceService rafflesPersistence;
    private final ITicketsService ticketsCreateService;
    private final IRafflesMapper rafflesMapper;
    private final ITokensQueryService tokensQueryService;
    private final IAssociationsService associationsService;
    private final IImagesService imagesService;

    @Value("${RAFFLE_CLIENT_HOST}")
    private String host;

    @Value("${RAFFLE_CLIENT_PATH}")
    private String path;

    @Transactional
    public PublicRaffleDTO createRaffle(String token, RaffleCreate request) {
        String identifier = tokensQueryService.getSubject(token);
        Association association = associationsService.findByIdentifier(identifier);
        Raffle raffle = rafflesMapper.toRaffle(request, association);
        Raffle savedRaffle = rafflesPersistence.save(raffle);
        raffle.setURL(host + path + raffle.getId());
        raffle.setImages(imagesService.create(raffle, request.images()));
        raffle.setTickets(ticketsCreateService.create(raffle, request.ticketsInfo()));
        return rafflesMapper.fromRaffle(rafflesPersistence.save(savedRaffle));
    }
}
