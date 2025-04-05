package com.raffleease.raffleease.Domains.Images.DTOs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

import static com.raffleease.raffleease.Constants.ImagesConstraints.MAX_IMAGES;
import static com.raffleease.raffleease.Constants.ImagesConstraints.MIN_IMAGES;

@Builder
public record UpdateOrderRequest(
        @NotEmpty(message = "Must provide images to reorder")
        @Size(min = MIN_IMAGES, max = MAX_IMAGES, message = "A minimum of 1 and a maximum of 10 images are allowed")
        @Valid
        List<ImageDTO> images
) { }
