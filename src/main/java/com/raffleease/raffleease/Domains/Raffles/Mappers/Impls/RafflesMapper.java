package com.raffleease.raffleease.Domains.Raffles.Mappers.Impls;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import com.raffleease.raffleease.Domains.Associations.Mappers.Impl.AssociationsMapper;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.Mappers.ImagesMapper;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Mappers.IRafflesMapper;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus.PENDING;
import static java.math.BigDecimal.ZERO;

@RequiredArgsConstructor
@Service
public class RafflesMapper implements IRafflesMapper {
    private final ImagesMapper imagesMapper;
    private final AssociationsMapper associationsMapper;

    @Override
    public Raffle toRaffle(RaffleCreate raffleData, Association association) {
        return Raffle.builder()
                .title(raffleData.title())
                .description(raffleData.description())
                .endDate(raffleData.endDate())
                .status(PENDING)
                .ticketPrice(raffleData.ticketsInfo().price())
                .availableTickets(raffleData.ticketsInfo().amount())
                .totalTickets(raffleData.ticketsInfo().amount())
                .firstTicketNumber(raffleData.ticketsInfo().lowerLimit())
                .association(association)
                .soldTickets(0L)
                .revenue(ZERO)
                .build();
    }

    @Override
    public PublicRaffleDTO fromRaffle(Raffle raffle) {
        AssociationDTO association = associationsMapper.fromAssociation(raffle.getAssociation());

        List<ImageDTO> images = imagesMapper.fromImagesList(raffle.getImages()).stream()
                .sorted(Comparator.comparing(ImageDTO::imageOrder)).toList();

        return PublicRaffleDTO.builder()
                .id(raffle.getId())
                .title(raffle.getTitle())
                .description(raffle.getDescription())
                .url(raffle.getURL())
                .startDate(raffle.getStartDate())
                .endDate(raffle.getEndDate())
                .status(raffle.getStatus())
                .images(images)
                .ticketPrice(raffle.getTicketPrice())
                .firstTicketNumber(raffle.getFirstTicketNumber())
                .soldTickets(raffle.getSoldTickets())
                .revenue(raffle.getRevenue())
                .availableTickets(raffle.getAvailableTickets())
                .totalTickets(raffle.getTotalTickets())
                .association(association)
                .build();
    }

    @Override
    public List<PublicRaffleDTO> fromRaffleList(List<Raffle> raffles) {
        return raffles.stream().map(this::fromRaffle).toList();
    }
}