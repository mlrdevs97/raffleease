package com.raffleease.raffleease.Domains.Tickets.Services;

import com.raffleease.raffleease.Domains.Tickets.DTO.*;
import jakarta.validation.Valid;

import java.util.List;

public interface ITicketsOrchestrator {

    List<TicketDTO> findByTicketNumber(@Valid SearchRequest request);

    void release(@Valid ReservationRequest request);

    ReservationResponse generateRandom(@Valid GenerateRandom request);

    ReservationResponse reserve(@Valid ReservationRequest request);
}
