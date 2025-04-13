package com.raffleease.raffleease.Domains.Raffles.Mappers;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;

import java.util.List;

public interface IRafflesMapper {
    Raffle toRaffle(RaffleCreate request, Association association);
    PublicRaffleDTO fromRaffle(Raffle raffle);
    List<PublicRaffleDTO> fromRaffleList(List<Raffle> raffles);
}
