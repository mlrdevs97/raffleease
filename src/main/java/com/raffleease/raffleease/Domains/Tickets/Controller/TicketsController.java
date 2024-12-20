package com.raffleease.raffleease.Domains.Tickets.Controller;

import com.raffleease.raffleease.Domains.Tickets.DTO.*;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketsController {
    private final ITicketsOrchestrator orchestrator;

    @GetMapping("/search")
    public ResponseEntity<List<TicketDTO>> findByTicketNumber(
            @RequestParam("raffleId") Long raffleId,
            @RequestParam("ticketNumber") String ticketNumber
    ) {
        return ResponseEntity.ok(orchestrator.findByTicketNumber(raffleId, ticketNumber));
    }

    @PostMapping("/reservations/random")
    public ResponseEntity<ReservationResponse> generateRandom(
            @Valid @RequestBody GenerateRandom request
    ) {
        return ResponseEntity.ok(orchestrator.generateRandom(request));
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> reserve(
            @Valid @RequestBody ReservationRequest request
    ) {
        return ResponseEntity.ok(orchestrator.reserve(request));
    }

    @DeleteMapping("/reservations")
    public ResponseEntity<Void> release(
            @Valid @RequestBody ReservationRequest request
    ) {
        orchestrator.release(request);
        return ResponseEntity.noContent().build();
    }
}
