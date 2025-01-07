package com.raffleease.raffleease.Domains.Tickets.Controller;

import com.raffleease.raffleease.Domains.Tickets.DTO.*;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsQueryService;
import com.raffleease.raffleease.Responses.ApiResponse;
import com.raffleease.raffleease.Responses.ResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketsController {
    private final ITicketsQueryService queryService;

    @GetMapping
    public ResponseEntity<ApiResponse> findByTicketNumber(
            @RequestParam("raffleId") Long raffleId,
            @RequestParam("ticketNumber") String ticketNumber
    ) {
        List<TicketDTO> tickets = queryService.findByTicketNumber(raffleId, ticketNumber);
        return ResponseEntity.ok(
                ResponseFactory.success(
                        tickets,
                        "Tickets retrieved successfully"
                )
        );
    }
}
