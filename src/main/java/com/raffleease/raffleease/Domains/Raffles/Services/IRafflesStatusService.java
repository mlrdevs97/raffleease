package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
public interface IRafflesStatusService {
    PublicRaffleDTO publish(Long id);
    PublicRaffleDTO pause(Long id);
    PublicRaffleDTO restart(Long id);
    void delete(Long id);
}
