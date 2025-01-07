package com.raffleease.raffleease.Domains.Raffles.Mappers.Impls;

import com.raffleease.raffleease.Domains.Associations.Mappers.Impl.AssociationsMapper;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Images.Mappers.IImagesMapper;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Mappers.IRafflesMapper;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus.PENDING;

@RequiredArgsConstructor
@Service
public class RafflesMapper implements IRafflesMapper {
    private final IImagesMapper imagesMapper;
    private final AssociationsMapper associationsMapper;

    public Raffle toRaffle(RaffleCreate request, Association association) {
        return Raffle.builder()
                .title(request.title())
                .description(request.description())
                .endDate(request.endDate())
                .status(PENDING)
                .ticketPrice(request.ticketsInfo().price())
                .availableTickets(request.ticketsInfo().amount())
                .totalTickets(request.ticketsInfo().amount())
                .firstTicketNumber(request.ticketsInfo().lowerLimit())
                .association(association)
                .build();
    }

    public PublicRaffleDTO fromRaffle(Raffle raffle) {
        return PublicRaffleDTO.builder()
                .id(raffle.getId())
                .title(raffle.getTitle())
                .description(raffle.getDescription())
                .url(raffle.getURL())
                .startDate(raffle.getStartDate())
                .endDate(raffle.getEndDate())
                .status(raffle.getStatus())
                .images(imagesMapper.fromImagesList(raffle.getImages()))
                .ticketPrice(raffle.getTicketPrice())
                .firstTicketNumber(raffle.getFirstTicketNumber())
                .soldTickets(raffle.getSoldTickets())
                .revenue(raffle.getRevenue())
                .availableTickets(raffle.getAvailableTickets())
                .totalTickets(raffle.getTotalTickets())
                .association(associationsMapper.fromAssociation(raffle.getAssociation()))
                .build();
    }

    public List<PublicRaffleDTO> fromRaffleList(List<Raffle> raffles) {
        return raffles.stream().map(this::fromRaffle).toList();
    }
}
