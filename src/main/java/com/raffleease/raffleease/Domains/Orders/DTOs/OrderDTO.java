package com.raffleease.raffleease.Domains.Orders.DTOs;

import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Customers.DTO.CustomerDTO;
import com.raffleease.raffleease.Domains.Payments.DTOs.PaymentDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Builder
public record OrderDTO(
        @Builder
        @NotNull(message = "Order ID cannot be null")
        Long id,

        @NotNull(message = "Order reference cannot be null")
        @Size(min = 20, max = 40, message = "Amount of characters not valid")
        String orderReference,

        @NotNull(message = "Order's cart is required")
        @Validated
        CartDTO cart,

        @NotNull(message = "Order's payment is required")
        @Validated
        PaymentDTO payment,

        @Validated
        CustomerDTO customer,

        @NotNull(message = "Order date cannot be null")
        @PastOrPresent(message = "Order date cannot be in the future")
        LocalDateTime orderDate
) {
}
