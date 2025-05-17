package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleSearchFilters;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RafflesQueryService {
    PublicRaffleDTO get(Long id);
    List<PublicRaffleDTO> search(Long associationId);
    List<Raffle> findAllByAssociation(Association association);
    Page<PublicRaffleDTO> search(Long associationId, RaffleSearchFilters filters, Pageable pageable);
}
