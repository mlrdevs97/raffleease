package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Common.Configs.CorsProperties;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Services.ImagesAssociateService;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Mappers.RafflesMapper;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatistics;
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
import java.util.ArrayList;
import java.util.List;

import static com.raffleease.raffleease.Domains.Raffles.Model.CompletionReason.ALL_TICKETS_SOLD;
import static com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus.*;
import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.SOLD;
import static java.math.BigDecimal.ZERO;

@RequiredArgsConstructor
@Service
public class RafflesServiceImpl implements RafflesService {
    private final RafflesPersistenceService rafflesPersistence;
    private final TicketsService ticketsCreateService;
    private final RafflesMapper rafflesMapper;
    private final AssociationsService associationsService;
    private final ImagesAssociateService imagesAssociateService;
    private final CorsProperties corsProperties;

    @Override
    @Transactional
    public RaffleDTO create(Long associationId, RaffleCreate raffleData) {
        Association association = associationsService.findById(associationId);
        Raffle newRaffle = createNewRaffle(raffleData, association);
        RaffleStatistics statistics = createNewStatistics(newRaffle, raffleData.ticketsInfo().amount());
        newRaffle.setStatistics(statistics);
        Raffle raffle = rafflesPersistence.save(newRaffle);
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
            raffle.setStatus(COMPLETED);
            raffle.setCompletedAt(LocalDateTime.now());
            raffle.setCompletionReason(ALL_TICKETS_SOLD);
        }
        rafflesPersistence.save(raffle);
    }

    @Override
    public void reactivateRaffleIfAllTicketsSold(Raffle raffle) {
        if (raffle.getStatus().equals(COMPLETED) && raffle.getCompletionReason().equals(ALL_TICKETS_SOLD)) {
            raffle.setStatus(ACTIVE);
            raffle.setCreatedAt(null);
            raffle.setCompletionReason(null);
        }
        rafflesPersistence.save(raffle);
    }

    private Raffle createNewRaffle(RaffleCreate raffleData, Association association) {
        return Raffle.builder()
                .association(association)
                .title(raffleData.title())
                .description(raffleData.description())
                .status(PENDING)
                .ticketPrice(raffleData.ticketsInfo().price())
                .totalTickets(raffleData.ticketsInfo().amount())
                .firstTicketNumber(raffleData.ticketsInfo().lowerLimit())
                .images(new ArrayList<>())
                .tickets(new ArrayList<>())
                .orders(new ArrayList<>())
                .startDate(raffleData.startDate())
                .endDate(raffleData.endDate())
                .build();
    }

    private RaffleStatistics createNewStatistics(Raffle raffle, long ticketAmount) {
        return RaffleStatistics.builder()
                .raffle(raffle)
                .availableTickets(ticketAmount)
                .soldTickets(0L)
                .closedSells(0L)
                .failedSells(0L)
                .refundTickets(0L)
                .unpaidTickets(0L)
                .revenue(ZERO)
                .build();
    }
}
