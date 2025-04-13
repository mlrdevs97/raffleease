package com.raffleease.raffleease.Domains.Orders.Controller;

import com.raffleease.raffleease.Domains.Auth.Validations.ValidateAssociationAccess;
import com.raffleease.raffleease.Domains.Orders.Services.OrdersService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.CartHeaderMissingException;
import com.raffleease.raffleease.Responses.ApiResponse;
import com.raffleease.raffleease.Responses.ResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@ValidateAssociationAccess
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/api/v1/orders")
public class AdminOrdersController {
    private final OrdersService ordersCreateService;

    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(
            HttpServletRequest httpRequest
    ) {
        String cartId = "";
        if (Objects.isNull(cartId)) throw new CartHeaderMissingException("Cannot complete order because the cart ID is missing");

        String sessionKey = ordersCreateService.create(Long.parseLong(cartId));
        return ResponseEntity.ok(
                ResponseFactory.success(
                        sessionKey,
                        "Checkout session created successfully"
                )
        );
    }
}
