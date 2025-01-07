package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Services.IImagesService;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleEdit;
import com.raffleease.raffleease.Domains.Raffles.Mappers.IRafflesMapper;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesEditService;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesPersistenceService;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketsCreate;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RafflesEditServiceImpl implements IRafflesEditService {
    private final IRafflesPersistenceService rafflesPersistence;
    private final ITicketsService ticketsCreateService;
    private final IImagesService imagesService;
    private final IRafflesMapper mapper;

    @Transactional
    public PublicRaffleDTO edit(Long id, RaffleEdit raffleEdit) {
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

        if (raffleEdit.deleteImageIds() != null && !raffleEdit.deleteImageIds().isEmpty()) {
            deleteImages(raffle, raffleEdit.deleteImageIds());
        }

        if (raffleEdit.newIMages() != null && !raffleEdit.newIMages().isEmpty()) {
            addNewImages(raffle, raffleEdit.newIMages());
        }

        if (raffleEdit.ticketPrice() != null) {
            raffle.setTicketPrice(raffleEdit.ticketPrice());
        }

        if (raffleEdit.totalTickets() != null) {
            editTotalTickets(raffle, raffleEdit.totalTickets());
        }

        Raffle savedRaffle = rafflesPersistence.save(raffle);
        return mapper.fromRaffle(savedRaffle);
    }

    @Override
    public void edit(Raffle raffle, BigDecimal revenue, Long soldTickets) {
        raffle.setSoldTickets(raffle.getSoldTickets() + soldTickets);
        raffle.setRevenue(raffle.getRevenue().add(revenue));
        rafflesPersistence.save(raffle);
    }

    private void deleteImages(Raffle raffle, List<Long> deleteIds) {
        List<Image> currentImages = raffle.getImages();

        List<Image> imagesToDelete = currentImages.stream()
                .filter(image -> deleteIds.contains(image.getId()))
                .toList();

        if (currentImages.size() - imagesToDelete.size() < 1) {
            throw new BusinessException("At least one picture for raffle is required");
        }

        imagesService.deleteAll(imagesToDelete);
        raffle.getImages().removeAll(imagesToDelete);
    }

    private void addNewImages(Raffle raffle, List<MultipartFile> newImages) {
        List<Image> currentImages = raffle.getImages();

        if (currentImages.size() + newImages.size() >= 10) {
            throw new BusinessException("A maximum of 10 pictures are allowed");
        }

        List<Image> createdImages = imagesService.create(raffle, newImages);
        raffle.getImages().addAll(createdImages);
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
            createAdditionalTickets(raffle, ticketDifference);
        }
    }

    private void createAdditionalTickets(Raffle raffle, long amount) {
        long lowerLimit = raffle.getFirstTicketNumber() + raffle.getTotalTickets();

        TicketsCreate request = TicketsCreate.builder()
                .amount(amount)
                .price(raffle.getTicketPrice())
                .lowerLimit(lowerLimit)
                .build();

        raffle.getTickets().addAll(ticketsCreateService.create(raffle, request));
    }
}
