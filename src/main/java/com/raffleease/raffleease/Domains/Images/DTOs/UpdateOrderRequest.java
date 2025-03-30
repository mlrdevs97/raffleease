package com.raffleease.raffleease.Domains.Images.DTOs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record UpdateOrderRequest(
        @NotEmpty(message = "Must provide images to reorder")
        @Valid
        List<ImageDTO> images
) { }
