package com.raffleease.raffleease.Domains.Orders.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AddCommentRequest(
        @NotNull(message = "Comment cannot be null")
        @Size(max = 500, message = "Comment must not exceed 500 characters")
        String comment
) {}
