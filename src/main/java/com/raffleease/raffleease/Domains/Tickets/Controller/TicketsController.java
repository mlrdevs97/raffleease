package com.raffleease.raffleease.Domains.Tickets.Controller;

import com.raffleease.raffleease.Domains.Tickets.DTO.*;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketsController {
    private final ITicketsOrchestrator orchestrator;

    @PostMapping("/find-by-number")
    public ResponseEntity<List<TicketDTO>> findByTicketNumber(
            @Valid @RequestBody SearchRequest request
    ) {
        return ResponseEntity.ok(orchestrator.findByTicketNumber(request));
    }

    @PostMapping("/generate-random")
    public ResponseEntity<ReservationResponse> generateRandom(
            @Valid @RequestBody GenerateRandom request
    ) {
        return ResponseEntity.ok(orchestrator.generateRandom(request));
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserve(
            @Valid @RequestBody ReservationRequest request
    ) {
        return ResponseEntity.ok(orchestrator.reserve(request));
    }

    @PostMapping("/release")
    public ResponseEntity<Void> release(
            @Valid @RequestBody ReservationRequest request
    ) {
        orchestrator.release(request);
        return ResponseEntity.noContent().build();
    }
}
