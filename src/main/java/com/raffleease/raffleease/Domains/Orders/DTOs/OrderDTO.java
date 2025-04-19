package com.raffleease.raffleease.Domains.Orders.DTOs;

import com.raffleease.raffleease.Domains.Customers.DTO.CustomerDTO;
import com.raffleease.raffleease.Domains.Orders.Model.OrderSource;
import com.raffleease.raffleease.Domains.Orders.Model.OrderStatus;
import com.raffleease.raffleease.Domains.Payments.DTOs.PaymentDTO;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderDTO(
        @NotNull(message = "Order ID cannot be null")
        Long id,

        @NotNull(message = "Order reference cannot be null")
        @Size(min = 20, max = 40, message = "Amount of characters not valid")
        String orderReference,

        @NotNull(message = "Orden source cannot be null")
        OrderSource orderSource,

        @NotNull(message = "Payment status cannot be null")
        OrderStatus status,

        @NotEmpty(message = "Order items are required")
        @Valid
        List<OrderItemDTO> orderItems,

        @NotNull(message = "Order's payment is required")
        @Valid
        PaymentDTO payment,

        @Valid
        CustomerDTO customer,

        @Nullable
        @Size(max = 500, message = "Comment must not exceed 500 characters")
        String comment,

        @NotNull(message = "Order date cannot be null")
        @PastOrPresent(message = "Order date cannot be in the future")
        LocalDateTime createdAt,

        @PastOrPresent(message = "Order date cannot be in the future")
        LocalDateTime updatedAt,
        LocalDateTime completedAt,
        LocalDateTime cancelledAt
) { }
