package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesOrchestrator;
import com.raffleease.raffleease.Responses.ApiResponse;
import com.raffleease.raffleease.Responses.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/raffles")
public class RafflesController {
    private final IRafflesOrchestrator orchestrator;

    @PostMapping
    public ResponseEntity<ApiResponse> create(
            @Valid @RequestBody RaffleCreate request
    ) {
        RaffleDTO createdRaffle = orchestrator.createRaffle(request);
        return ResponseEntity.ok(
                ResponseFactory.success(
                        createdRaffle,
                        "New raffle successfully created"
                )
        );
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<ApiResponse> publish(
            @PathVariable Long id
    ) {
        RaffleDTO publishedRaffle = orchestrator.publish(id);
        return ResponseEntity.ok(ResponseFactory.success(
                publishedRaffle,
                "Raffle successfully published"
        ));
    }

    @PatchMapping("/{id}/pause")
    public ResponseEntity<ApiResponse> pause(
            @PathVariable Long id
    ) {
        RaffleDTO pausedRaffle = orchestrator.pause(id);
        return ResponseEntity.ok(ResponseFactory.success(
                pausedRaffle,
                "Raffle successfully paused"
        ));
    }

    @PatchMapping("/{id}/restart")
    public ResponseEntity<ApiResponse> restart(
            @PathVariable Long id
    ) {
        RaffleDTO restartedRaffle = orchestrator.restart(id);
        return ResponseEntity.ok(ResponseFactory.success(
                restartedRaffle,
                "Raffle successfully restarted"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> get(
            @PathVariable Long id
    ) {
        RaffleDTO retrievedRaffle = orchestrator.get(id);
        return ResponseEntity.ok(ResponseFactory.success(
                retrievedRaffle,
                "Raffle successfully retrieved"
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAll() {
        Set<RaffleDTO> retrievedRaffles = orchestrator.getAll();
        return ResponseEntity.ok(ResponseFactory.success(
                retrievedRaffles,
                "Raffles successfully published"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(
            @PathVariable Long id
    ) {
        orchestrator.delete(id);
        return ResponseEntity.ok(ResponseFactory.success(
                null,
                "Raffle successfully deleted"
        ));
    }
}
