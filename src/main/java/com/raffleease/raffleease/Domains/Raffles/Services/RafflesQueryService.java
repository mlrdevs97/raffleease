package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleSearchFilters;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RafflesQueryService {
    RaffleDTO get(Long id);
    List<RaffleDTO> search(Long associationId);
    List<Raffle> findAllByAssociation(Association association);
    Page<RaffleDTO> search(Long associationId, RaffleSearchFilters filters, Pageable pageable);
}
