package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Services.ImagesAssociateService;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleEdit;
import com.raffleease.raffleease.Domains.Raffles.Mappers.IRafflesMapper;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesEditService;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesPersistenceService;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketsCreate;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Services.TicketsService;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RafflesEditServiceImpl implements RafflesEditService {
    private final RafflesPersistenceService rafflesPersistence;
    private final TicketsService ticketsCreateService;
    private final ImagesAssociateService imagesAssociateService;
    private final IRafflesMapper rafflesMapper;

    @Transactional
    public RaffleDTO edit(Long id, RaffleEdit raffleEdit) {
        Raffle raffle = rafflesPersistence.findById(id);

        if (raffleEdit.title() != null) {
            raffle.setTitle(raffleEdit.title());
        }

        if (raffleEdit.description() != null) {
            raffle.setDescription(raffleEdit.description());
        }

        if (raffleEdit.endDate() != null) {
            raffle.setEndDate(raffleEdit.endDate());
        }

        if (raffleEdit.images() != null && !raffleEdit.images().isEmpty()) {
            addNewImages(raffle, raffleEdit.images());
        }

        if (raffleEdit.ticketPrice() != null) {
            raffle.setTicketPrice(raffleEdit.ticketPrice());
        }

        if (raffleEdit.totalTickets() != null) {
            editTotalTickets(raffle, raffleEdit.totalTickets());
        }

        if (raffleEdit.price() != null) {
            raffle.setTicketPrice(raffleEdit.ticketPrice());
        }

        raffle.setUpdatedAt(LocalDateTime.now());
        Raffle savedRaffle = rafflesPersistence.save(raffle);
        return rafflesMapper.fromRaffle(savedRaffle);
    }

    @Override
    public void updateStatistics(Raffle raffle, BigDecimal revenue, Long soldTickets) {
        raffle.setSoldTickets(raffle.getSoldTickets() + soldTickets);
        raffle.setRevenue(raffle.getRevenue().add(revenue));
        rafflesPersistence.save(raffle);
    }

    private void addNewImages(Raffle raffle, List<ImageDTO> imageDTOs) {
        List<Image> images = imagesAssociateService.associateImagesToRaffleOnEdit(raffle, imageDTOs);
        raffle.getImages().clear();
        raffle.getImages().addAll(images);
    }

    private void editTotalTickets(Raffle raffle, long editTotal) {
        if (raffle.getSoldTickets() != null && editTotal < raffle.getSoldTickets()) {
            throw new BusinessException("The total tickets count cannot be less than the number of tickets already sold for this raffle");
        }

        long oldTotal = raffle.getTotalTickets();
        raffle.setTotalTickets(editTotal);

        long ticketDifference = editTotal - oldTotal;
        raffle.setAvailableTickets(raffle.getAvailableTickets() + ticketDifference);

        if (ticketDifference > 0) {
            createAdditionalTickets(raffle, oldTotal, ticketDifference);
        }
    }

    private void createAdditionalTickets(Raffle raffle, long oldTotal, long amount) {
        long lowerLimit = raffle.getFirstTicketNumber() + oldTotal;

        TicketsCreate request = TicketsCreate.builder()
                .amount(amount)
                .price(raffle.getTicketPrice())
                .lowerLimit(lowerLimit)
                .build();

        List<Ticket> newTickets = ticketsCreateService.create(raffle, request);
        raffle.getTickets().addAll(newTickets);
    }
}