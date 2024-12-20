package com.raffleease.raffleease.Domains.Tickets.Services.Impls;

import com.raffleease.raffleease.Domains.Tickets.DTO.*;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketRandomService;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketReservationsService;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsOrchestrator;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TicketsOrchestratorImpl implements ITicketsOrchestrator {
    private final ITicketsQueryService ticketsQueryService;
    private final ITicketReservationsService reservationService;
    private final ITicketRandomService randomTicketsService;


    @Override
    public List<TicketDTO> findByTicketNumber(Long raffleId, String ticketNumber) {
        return ticketsQueryService.findByTicketNumber(raffleId, ticketNumber);
    }

    @Override
    public void release(ReservationRequest request) {
        reservationService.release(request);
    }

    @Override
    public ReservationResponse generateRandom(GenerateRandom request) {
        return randomTicketsService.generateRandom(request);
    }

    @Override
    public ReservationResponse reserve(ReservationRequest request) {
        return reservationService.reserve(request);
    }
}
