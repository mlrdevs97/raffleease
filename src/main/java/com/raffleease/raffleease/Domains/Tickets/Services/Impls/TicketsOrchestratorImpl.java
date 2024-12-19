package com.raffleease.raffleease.Domains.Tickets.Services.Impls;

import com.raffleease.raffleease.Domains.Tickets.DTO.*;
import com.raffleease.raffleease.Domains.Tickets.Services.IRandomTicketsService;
import com.raffleease.raffleease.Domains.Tickets.Services.IReservationsService;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsOrchestrator;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TicketsOrchestratorImpl implements ITicketsOrchestrator {
    private final ITicketsQueryService ticketsQueryService;
    private final IReservationsService reservationService;
    private final IRandomTicketsService randomTickets;


    @Override
    public List<TicketDTO> findByTicketNumber(SearchRequest request) {
        return ticketsQueryService.findByTicketNumber(request);
    }

    @Override
    public void release(ReservationRequest request) {
        reservationService.release(request);
    }

    @Override
    public ReservationResponse generateRandom(GenerateRandom request) {
        return randomTickets.generateRandom(request);
    }

    @Override
    public ReservationResponse reserve(ReservationRequest request) {
        return reservationService.reserve(request);
    }
}
