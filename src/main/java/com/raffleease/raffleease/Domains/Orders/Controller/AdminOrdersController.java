package com.raffleease.raffleease.Domains.Orders.Controller;

import com.raffleease.raffleease.Domains.Auth.Validations.ValidateAssociationAccess;
import com.raffleease.raffleease.Domains.Orders.DTOs.*;
import com.raffleease.raffleease.Domains.Orders.Services.OrdersCreateService;
import com.raffleease.raffleease.Domains.Orders.Services.OrdersEditService;
import com.raffleease.raffleease.Domains.Orders.Services.OrdersService;
import com.raffleease.raffleease.Common.Responses.ApiResponse;
import com.raffleease.raffleease.Common.Responses.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@ValidateAssociationAccess
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/api/v1/associations/{associationId}/orders")
public class AdminOrdersController {
    private final OrdersService ordersService;
    private final OrdersCreateService ordersCreateService;
    private final OrdersEditService ordersEditService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(
            @PathVariable Long associationId,
            @Valid @RequestBody AdminOrderCreate adminOrderCreate
    ) {
        OrderDTO order = ordersCreateService.create(adminOrderCreate, associationId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(order.id())
                .toUri();

        return ResponseEntity.created(location).body(
                ResponseFactory.success(
                        order,
                        "New order created successfully"
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse> search(
            @PathVariable Long associationId,
            @Valid OrderSearchFilters filters,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                ordersService.search(filters, associationId, pageable),
                "Orders retrieved successfully")
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> get(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                ordersService.get(orderId),
                "Order retrieved successfully"
        ));
    }

    @PutMapping("/{orderId}/complete")
    public ResponseEntity<ApiResponse> completeOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderComplete orderComplete
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                ordersEditService.completeOrder(orderId, orderComplete),
                "Order completed successfully"
        ));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse> updateStatus(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                ordersEditService.cancelOrder(orderId),
                "Order cancelled successfully"
        ));
    }

    @PatchMapping("/{orderId}/comment")
    public ResponseEntity<ApiResponse> addComment(
            @PathVariable Long orderId,
            @Valid @RequestBody AddCommentRequest request
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                ordersEditService.addComment(orderId, request),
                "Order comment added successfully"
        ));
    }
}