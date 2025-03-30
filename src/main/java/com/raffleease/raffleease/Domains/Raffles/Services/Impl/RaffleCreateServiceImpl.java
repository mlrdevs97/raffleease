package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Services.ImagesService;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Mappers.IRafflesMapper;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.RaffleCreateService;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesPersistenceService;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RaffleCreateServiceImpl implements RaffleCreateService {
    private final RafflesPersistenceService rafflesPersistence;
    private final ITicketsService ticketsCreateService;
    private final IRafflesMapper rafflesMapper;
    private final AssociationsService associationsService;
    private final ImagesService imagesService;

    @Value("${spring.application.host.client}")
    private String host;

    @Transactional
    public PublicRaffleDTO create(HttpServletRequest request, RaffleCreate raffleData) {
        Association association = associationsService.findFromRequest(request);
        Raffle mappedRaffle = rafflesMapper.toRaffle(raffleData, association);
        Raffle raffle = rafflesPersistence.save(mappedRaffle);
        raffle.setURL(host + "/client/raffle/" + raffle.getId());
        List<Image> images = imagesService.associateImagesToRaffleOnCreate(raffle, raffleData.images());
        raffle.setImages(images);
        List<Ticket> tickets = ticketsCreateService.create(raffle, raffleData.ticketsInfo());
        raffle.setTickets(tickets);
        return rafflesMapper.fromRaffle(rafflesPersistence.save(raffle));
    }
}
