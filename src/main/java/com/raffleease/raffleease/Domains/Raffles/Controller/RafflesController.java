package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/raffles")
public class RafflesController {
    private final RafflesOrchestrator orchestrator;

    @PostMapping("/")
    public ResponseEntity<RaffleDTO> create(
            @Valid @RequestBody RaffleCreate request
    ) {
        return ResponseEntity.ok(orchestrator.createRaffle(request));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<RaffleDTO> publish(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orchestrator.publish(id));
    }

    @PatchMapping("/{id}/pause")
    public ResponseEntity<RaffleDTO> pause(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orchestrator.pause(id));
    }

    @PatchMapping("/{id}/restart")
    public ResponseEntity<RaffleDTO> restart(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orchestrator.restart(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RaffleDTO> get(
            @PathVariable long id
    ) {
        return ResponseEntity.ok(orchestrator.get(id));
    }

    @GetMapping
    public ResponseEntity<Page> getAll() {
        Page raffles = orchestrator.getAll();
        return ResponseEntity.ok(raffles);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable long id
    ) {
        orchestrator.delete(id);
        return ResponseEntity.ok().build();
    }
}
