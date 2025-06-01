package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Common.Configs.CorsProperties;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Services.ImagesAssociateService;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Mappers.IRafflesMapper;
import com.raffleease.raffleease.Domains.Raffles.Model.CompletionReason;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesService;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesPersistenceService;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Services.TicketsService;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.SOLD;

@RequiredArgsConstructor
@Service
public class RafflesServiceImpl implements RafflesService {
    private final RafflesPersistenceService rafflesPersistence;
    private final TicketsService ticketsCreateService;
    private final IRafflesMapper rafflesMapper;
    private final AssociationsService associationsService;
    private final ImagesAssociateService imagesAssociateService;
    private final CorsProperties corsProperties;

    @Override
    @Transactional
    public RaffleDTO create(Long associationId, RaffleCreate raffleData) {
        Association association = associationsService.findById(associationId);
        Raffle mappedRaffle = rafflesMapper.toRaffle(raffleData, association);
        Raffle raffle = rafflesPersistence.save(mappedRaffle);
        raffle.setURL(corsProperties.getClientAsList().get(0) + "/client/raffle/" + raffle.getId());
        List<Image> images = imagesAssociateService.associateImagesToRaffleOnCreate(raffle, raffleData.images());
        raffle.setImages(images);
        List<Ticket> tickets = ticketsCreateService.create(raffle, raffleData.ticketsInfo());
        raffle.setTickets(tickets);
        return rafflesMapper.fromRaffle(rafflesPersistence.save(raffle));
    }

    @Override
    public void delete(Long id) {
        Raffle raffle = rafflesPersistence.findById(id);
        if (!raffle.getStatus().equals(RaffleStatus.PENDING)) {
            throw new BusinessException("Only raffles in 'PENDING' state can be deleted.");
        }
        rafflesPersistence.delete(raffle);
    }

    @Override
    public void completeRaffleIfAllTicketsSold(Raffle raffle) {
        boolean allTicketsSold = raffle.getTickets().stream().allMatch(ticket -> ticket.getStatus().equals(SOLD));
        if (allTicketsSold) {
            raffle.setStatus(RaffleStatus.COMPLETED);
            raffle.setCompletedAt(LocalDateTime.now());
            raffle.setCompletionReason(CompletionReason.ALL_TICKETS_SOLD);
        }
        rafflesPersistence.save(raffle);
    }
}
