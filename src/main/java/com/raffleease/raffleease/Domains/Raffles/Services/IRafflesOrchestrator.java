package com.raffleease.raffleease.Domains.Raffles.Services;

    import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
    import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleEdit;

    import java.util.Set;

public interface IRafflesOrchestrator {
    void delete(Long id);
    RaffleDTO publish(Long id);
    RaffleDTO pause(Long id);
    RaffleDTO restart(Long id);
    RaffleDTO get(Long id);
    Set<RaffleDTO> getAll();
    RaffleDTO createRaffle(RaffleCreate request);
    public RaffleDTO edit(Long id, RaffleEdit editRaffle);
}
