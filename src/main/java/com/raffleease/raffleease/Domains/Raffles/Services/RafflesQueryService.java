package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;

import java.util.List;

public interface RafflesQueryService {
    PublicRaffleDTO get(Long id);
    List<PublicRaffleDTO> getAll(Long associationId);
    List<Raffle> findAllByAssociation(Association association);
}
