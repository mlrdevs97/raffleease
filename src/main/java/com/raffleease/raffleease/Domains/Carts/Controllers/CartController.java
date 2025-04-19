package com.raffleease.raffleease.Domains.Carts.Controllers;

import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Carts.Services.CartsService;
import com.raffleease.raffleease.Domains.Carts.DTO.ReservationRequest;
import com.raffleease.raffleease.Domains.Carts.Services.ReservationsService;
import com.raffleease.raffleease.Responses.ApiResponse;
import com.raffleease.raffleease.Responses.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/associations/{associationId}/carts")
public class CartController {
    private final CartsService cartsService;
    private final ReservationsService reservationsService;

    @PostMapping
    public ResponseEntity<ApiResponse> create() {
        CartDTO createdCart = cartsService.createCustomerCart();

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

    @PostMapping("/{id}/reservations")
    public ResponseEntity<ApiResponse> reserve(
            @PathVariable Long cartId,
            @Valid @RequestBody ReservationRequest reservationRequest
    ) {
        CartDTO cartDTO = reservationsService.reserve(reservationRequest, cartId);
        return ResponseEntity.ok(
                ResponseFactory.success(
                        cartDTO,
                        "New reservation generated successfully"
                )
        );
    }

    @PutMapping("/{id}/reservations")
    public ResponseEntity<ApiResponse> release(
            @PathVariable Long cartId,
            @Valid @RequestBody ReservationRequest request
    ) {
        reservationsService.release(request, cartId);
        return ResponseEntity.ok().body(ResponseFactory.success("Tickets released successfully"));
    }
}
