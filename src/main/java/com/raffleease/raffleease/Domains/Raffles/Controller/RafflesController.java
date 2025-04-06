package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleEdit;
import com.raffleease.raffleease.Domains.Raffles.DTOs.StatusUpdate;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesEditService;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesQueryService;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesService;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesStatusService;
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
    private final RafflesService rafflesService;
    private final RafflesEditService rafflesEditService;
    private final RafflesStatusService rafflesStatusService;
    private final RafflesQueryService rafflesQueryService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(
            HttpServletRequest request,
            @RequestBody @Valid RaffleCreate raffleData
    ) {
        PublicRaffleDTO createdRaffle = rafflesService.create(request, raffleData);

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
                rafflesEditService.edit(id, raffleEdit),
                "Raffle edited successfully"
        ));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdate request
            ) {
        return ResponseEntity.ok(ResponseFactory.success(
                rafflesStatusService.updateStatus(id, request),
                "Raffle status updated successfully"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> get(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                rafflesQueryService.getAll(id),
                "Raffle retrieved successfully"
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAll(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                rafflesQueryService.getAll(request),
                "All raffles retrieved successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        rafflesService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
