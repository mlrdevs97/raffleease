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
        @NotNull
        Long id,

        @NotNull
        @Size(min = 20, max = 40)
        String orderReference,

        @NotNull
        OrderSource orderSource,

        @NotNull
        OrderStatus status,

        @NotEmpty
        @Valid
        List<OrderItemDTO> orderItems,

        @NotNull
        @Valid
        PaymentDTO payment,

        @Valid
        CustomerDTO customer,

        @Nullable
        @Size(max = 500)
        String comment,

        @NotNull
        @PastOrPresent
        LocalDateTime createdAt,

        @PastOrPresent
        LocalDateTime updatedAt,
        LocalDateTime completedAt,
        LocalDateTime cancelledAt
) { }
