package com.raffleease.raffleease.Domains.Reservations.Controller;

import com.raffleease.raffleease.Domains.Cart.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Reservations.DTOs.ReleaseRequest;
import com.raffleease.raffleease.Domains.Reservations.Services.IReservationsCreateService;
import com.raffleease.raffleease.Domains.Reservations.DTOs.GenerateRandom;
import com.raffleease.raffleease.Domains.Reservations.DTOs.ReservationRequest;
import com.raffleease.raffleease.Domains.Reservations.Services.IReservationsRandomService;
import com.raffleease.raffleease.Domains.Reservations.Services.IReservationsReleaseService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.CartHeaderMissingException;
import com.raffleease.raffleease.Responses.ApiResponse;
import com.raffleease.raffleease.Responses.ResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Objects;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tickets/reservations")
public class ReservationsController {
    private final IReservationsRandomService randomService;
    private final IReservationsReleaseService releaseService;
    private final IReservationsCreateService reservationsService;

    @PostMapping("/random")
    public ResponseEntity<ApiResponse> generateRandom(
            @Valid @RequestBody GenerateRandom randomRequest,
            HttpServletRequest httpRequest
    ) {
        String cartId = (String) httpRequest.getAttribute("carId");
        CartDTO cartDTO = randomService.generateRandom(randomRequest, cartId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(cartDTO.cartId())
                .toUri();

        return ResponseEntity.created(location).body(
                ResponseFactory.success(
                        cartDTO,
                        "New random reservation generated successfully"
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse> reserve(
            HttpServletRequest httpRequest,
            @Valid @RequestBody ReservationRequest reservationRequest
    ) {
        String cartId = (String) httpRequest.getAttribute("carId");
        CartDTO cartDTO = reservationsService.reserve(reservationRequest, cartId);
        return ResponseEntity.ok(
                ResponseFactory.success(
                        cartDTO,
                        "New reservation generated successfully"
                )
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse> release(
            HttpServletRequest httpRequest,
            @Valid @RequestBody ReleaseRequest request
    ) {
        String cartId = (String) httpRequest.getAttribute("carId");
        if (Objects.isNull(cartId)) throw new CartHeaderMissingException("Cannot release tickets because the cart ID is missing");

        releaseService.release(request, Long.parseLong(cartId));
        return ResponseEntity.ok().body(ResponseFactory.success("Tickets released successfully"));
    }
}
