package com.raffleease.raffleease.Domains.Raffles.Mappers;

import com.raffleease.raffleease.Domains.Associations.Mappers.AssociationsMapper;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class RafflesMapper {
    private final ImagesMapper imagesMapper;
    private final AssociationsMapper associationsMapper;

    public Raffle toRaffle(RaffleCreate request) {
        return Raffle.builder()
                .title(request.title())
                .description(request.description())
                .endDate(request.endDate())
                .ticketPrice(request.ticketsInfo().price())
                .availableTickets(request.ticketsInfo().amount())
                .totalTickets(request.ticketsInfo().amount())
                .firstTicketNumber(request.ticketsInfo().lowerLimit())
                .build();
    }

    public RaffleDTO fromRaffle(Raffle raffle) {
        return RaffleDTO.builder()
                .id(raffle.getId())
                .title(raffle.getTitle())
                .description(raffle.getDescription())
                .url(raffle.getURL())
                .startDate(raffle.getStartDate())
                .endDate(raffle.getEndDate())
                .status(raffle.getStatus())
                .imageKeys(imagesMapper.fromImages(raffle.getImages()))
                .ticketPrice(raffle.getTicketPrice())
                .firstTicketNumber(raffle.getFirstTicketNumber())
                .availableTickets(raffle.getAvailableTickets())
                .totalTickets(raffle.getTotalTickets())
                .soldTickets(raffle.getSoldTickets())
                .revenue(raffle.getRevenue())
                .association(associationsMapper.fromAssociation(raffle.getAssociation()))
                .build();
    }

    public List<RaffleDTO> fromRaffleList(List<Raffle> raffles) {
        return raffles.stream().map(this::fromRaffle).toList();
    }
}
