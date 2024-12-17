package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/raffles")
public class RafflesController {
    private final RafflesOrchestrator orchestrator;

    @PostMapping("/")
    public ResponseEntity<RaffleDTO> create(
            @Valid @RequestBody RaffleCreate request,
            @RequestHeader("Authorization") String authHeader
    ) {
        return ResponseEntity.ok(orchestrator.createRaffle(request, authHeader));
    }

    // TODO
    @PutMapping("/publish/{id}")
    public ResponseEntity<RaffleDTO> publish(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orchestrator.publish(id));
    }

    // TODO
    @PutMapping("/pause/{id}")
    public ResponseEntity<RaffleDTO> pause(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orchestrator.pause(id));
    }

    // TODO
    @PutMapping("/restart/{id}")
    public ResponseEntity<RaffleDTO> restart(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orchestrator.restart(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RaffleDTO> edit(
            @PathVariable Long id,
            @Valid @RequestBody RaffleEdit editRaffle
    ) {
        return ResponseEntity.ok(orchestrator.edit(id, editRaffle));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RaffleDTO> get(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orchestrator.get(id));
    }

    @GetMapping("/")
    public ResponseEntity<Set<RaffleDTO>> getAll(
            @RequestHeader("Authorization") String authHeader
    ) {
        return ResponseEntity.ok(orchestrator.getAll(authHeader));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        deleteService.delete(id);
        return ResponseEntity.ok().build();
    }
}
