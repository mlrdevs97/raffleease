package com.raffleease.raffleease.Domains.Orders.Controller;

import com.raffleease.raffleease.Domains.Auth.Validations.ValidateAssociationAccess;
import com.raffleease.raffleease.Domains.Orders.DTOs.*;
import com.raffleease.raffleease.Domains.Orders.Services.OrdersCreateService;
import com.raffleease.raffleease.Domains.Orders.Services.OrdersEditService;
import com.raffleease.raffleease.Common.Responses.ApiResponse;
import com.raffleease.raffleease.Common.Responses.ResponseFactory;
import com.raffleease.raffleease.Domains.Orders.Services.OrdersQueryService;
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
@RequestMapping("/v1/associations/{associationId}/orders")
public class AdminOrdersController {
    private final OrdersQueryService ordersQueryService;
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
                ordersQueryService.search(filters, associationId, pageable),
                "Orders retrieved successfully")
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> get(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                ordersQueryService.get(orderId),
                "Order retrieved successfully"
        ));
    }

    @PutMapping("/{orderId}/complete")
    public ResponseEntity<ApiResponse> complete(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderComplete orderComplete
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                ordersEditService.complete(orderId, orderComplete),
                "Order completed successfully"
        ));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse> cancel(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                ordersEditService.cancel(orderId),
                "Order cancelled successfully"
        ));
    }

    @PutMapping("/{orderId}/refund")
    public ResponseEntity<ApiResponse> refund(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                ordersEditService.refund(orderId),
                "Order refunded successfully"
        ));
    }

    @PutMapping("/{orderId}/unpaid")
    public ResponseEntity<ApiResponse> setUnpaid(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                ordersEditService.setUnpaid(orderId),
                "Order unpaid successfully"
        ));
    }

    @PostMapping("/{orderId}/comment")
    public ResponseEntity<ApiResponse> addComment(
            @PathVariable Long orderId,
            @Valid @RequestBody CommentRequest request
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                ordersEditService.addComment(orderId, request),
                "Order comment added successfully"
        ));
    }

    @PutMapping("/{orderId}/comment")
    public ResponseEntity<ApiResponse> editComment(
            @PathVariable Long orderId,
            @Valid @RequestBody CommentRequest request
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                ordersEditService.addComment(orderId, request),
                "Order comment edited successfully"
        ));
    }

    @DeleteMapping("/{orderId}/comment")
    public ResponseEntity<ApiResponse> removeComment(
            @PathVariable Long orderId
    ) {
        ordersEditService.deleteComment(orderId);
        return ResponseEntity.noContent().build();
    }
}