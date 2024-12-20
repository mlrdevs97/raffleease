package com.raffleease.raffleease.Domains.Tickets.Services;

import com.raffleease.raffleease.Domains.Tickets.DTO.*;

import java.util.List;

public interface ITicketsOrchestrator {

    List<TicketDTO> findByTicketNumber(Long raffleId, String ticketNumber);

    void release(ReservationRequest request);

    ReservationResponse generateRandom(GenerateRandom request);

    ReservationResponse reserve(ReservationRequest request);
}
