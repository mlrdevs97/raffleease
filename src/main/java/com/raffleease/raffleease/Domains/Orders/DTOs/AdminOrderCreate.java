package com.raffleease.raffleease.Domains.Orders.DTOs;

import com.raffleease.raffleease.Domains.Customers.DTO.CustomerCreate;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record AdminOrderCreate(
        @NotNull(message = "Must provide cart id")
        @Positive(message = "Cart id must be a positive number")
        Long cartId,

        @NotEmpty(message = "At least one ticket is required to create an order")
        List<Long> ticketIds,

        @NotNull(message = "Must provide customer data")
        @Valid
        CustomerCreate customer,

        @Nullable
        @Size(max = 500, message = "Comment must not exceed 500 characters")
        String comment
) {
        public AdminOrderCreate {
                comment = trim(comment);
        }

        private static String trim(String value) {
                return value == null ? null : value.trim();
        }
}