package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
public interface IRafflesStatusService {
    RaffleDTO publish(Long id);
    RaffleDTO pause(Long id);
    RaffleDTO restart(Long id);
    void delete(Long id);
}
