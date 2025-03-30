package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleEdit;
import com.raffleease.raffleease.Domains.Raffles.DTOs.StatusUpdate;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesOrchestrator;
import com.raffleease.raffleease.Responses.ApiResponse;
import com.raffleease.raffleease.Responses.ResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/raffles")
public class RafflesController {
    private final RafflesOrchestrator orchestrator;

    @PostMapping
    public ResponseEntity<ApiResponse> create(
            HttpServletRequest request,
            @RequestBody @Valid RaffleCreate raffleData
    ) {
        PublicRaffleDTO createdRaffle = orchestrator.create(request, raffleData);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdRaffle.id())
                .toUri();

        return ResponseEntity.created(location).body(
                ResponseFactory.success(
                        createdRaffle,
                        "New raffle created successfully"
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> edit(
            @PathVariable Long id,
            @RequestBody @Valid RaffleEdit raffleEdit
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                orchestrator.edit(id, raffleEdit),
                "Raffle edited successfully"
        ));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse> publish(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdate request
            ) {
        return ResponseEntity.ok(ResponseFactory.success(
                orchestrator.updateStatus(id, request),
                "Raffle status updated successfully"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> get(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                orchestrator.getAll(id),
                "Raffle retrieved successfully"
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAll(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                orchestrator.getAll(request),
                "All raffles retrieved successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        orchestrator.delete(id);
        return ResponseEntity.noContent().build();
    }
}
