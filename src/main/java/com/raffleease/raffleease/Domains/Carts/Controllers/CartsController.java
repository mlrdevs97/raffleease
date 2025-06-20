package com.raffleease.raffleease.Domains.Carts.Controllers;

import com.raffleease.raffleease.Domains.Auth.Validations.ValidateAssociationAccess;
import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Carts.Services.CartsPersistenceService;
import com.raffleease.raffleease.Domains.Carts.Services.CartsService;
import com.raffleease.raffleease.Domains.Carts.DTO.ReservationRequest;
import com.raffleease.raffleease.Domains.Carts.Services.ReservationsService;
import com.raffleease.raffleease.Common.Responses.ApiResponse;
import com.raffleease.raffleease.Common.Responses.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@ValidateAssociationAccess
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/associations/{associationId}/carts")
public class CartsController {
    private final CartsService cartsService;
    private final ReservationsService reservationsService;
    private final CartsPersistenceService cartsPersistenceService;

    @PostMapping
    public ResponseEntity<ApiResponse> create() {
        CartDTO createdCart = cartsService.create();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCart.id())
                .toUri();

        return ResponseEntity.created(location).body(
                ResponseFactory.success(
                        createdCart,
                        "New cart created successfully"
                )
        );
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse> getUserActiveCart() {
        return ResponseEntity.ok(
                ResponseFactory.success(
                        cartsService.getUserCart(),
                        "Active user cart retrieved successfully"
                )
        );
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<ApiResponse> get(
            @PathVariable Long cartId
    ) {
        return ResponseEntity.ok(
                ResponseFactory.success(
                        cartsService.get(cartId),
                        "Cart retrieved successfully"
                )
        );
    }

    @PostMapping("/{cartId}/reservations")
    public ResponseEntity<ApiResponse> reserve(
            @PathVariable Long associationId,
            @PathVariable Long cartId,
            @Valid @RequestBody ReservationRequest reservationRequest
    ) {
        return ResponseEntity.ok(
                ResponseFactory.success(
                        reservationsService.reserve(reservationRequest, associationId, cartId),
                        "New reservation generated successfully"
                )
        );
    }

    @PutMapping("/{cartId}/reservations")
    public ResponseEntity<ApiResponse> release(
            @PathVariable Long associationId,
            @PathVariable Long cartId,
            @Valid @RequestBody ReservationRequest request
    ) {
        reservationsService.release(request, associationId, cartId);
        return ResponseEntity.ok().body(ResponseFactory.success("Tickets released successfully"));
    }
}