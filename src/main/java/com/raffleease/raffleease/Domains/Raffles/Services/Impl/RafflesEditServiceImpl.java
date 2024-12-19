package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleEdit;
import com.raffleease.raffleease.Domains.Raffles.Mappers.RafflesMapper;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleImage;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesCommandService;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesEditService;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesQueryService;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketsCreate;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsCreateService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class RafflesEditServiceImpl implements IRafflesEditService {
    private final IRafflesQueryService queryService;
    private final IRafflesCommandService commandService;
    private final ITicketsCreateService ticketsCreateService;
    private final RafflesMapper mapper;

    // private final S3Service s3Service;

    @Transactional
    public RaffleDTO edit(Long id, RaffleEdit editRaffle) {
        Raffle raffle = queryService.findById(id);

        if (editRaffle.title() != null) {
            raffle.setTitle(editRaffle.title());
        }

        if (editRaffle.description() != null) {
            raffle.setDescription(editRaffle.description());
        }

        if (editRaffle.endDate() != null) {
            raffle.setEndDate(editRaffle.endDate());
        }

        if (editRaffle.imageKeys() != null) {
            editImages(raffle, editRaffle.imageKeys());
        }

        if (editRaffle.ticketPrice() != null) {
            raffle.setTicketPrice(editRaffle.ticketPrice());
        }

        if (editRaffle.totalTickets() != null) {
            editTotalTickets(raffle, editRaffle.totalTickets());
        }

        Raffle savedRaffle = commandService.saveRaffle(raffle);
        return mapper.fromRaffle(savedRaffle);
    }

    private void editImages(Raffle raffle, List<String> newKeys) {
        List<RaffleImage> oldImages = raffle.getImages();

        List<String> oldKeys = oldImages.stream()
                .map(RaffleImage::getKey)
                .toList();

        List<String> deleteKeys = oldKeys.stream()
                .filter(k -> !newKeys.contains(k))
                .toList();

        if (!deleteKeys.isEmpty()) {
            raffle.getImages().removeIf(image -> deleteKeys.contains(image.getKey()));

            // s3Service.delete(deleteKeys);
        }

        List<String> addKeys = new ArrayList<>(newKeys);
        addKeys.removeAll(oldKeys);

        for (String newKey : addKeys) {
            RaffleImage newImage = RaffleImage.builder()
                    .key(newKey)
                    .raffle(raffle)
                    .build();
            raffle.getImages().add(newImage);
        }
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

        Set<Ticket> newTickets = ticketsCreateService.createTickets(request);
        raffle.getTickets().addAll(newTickets);
    }
}
