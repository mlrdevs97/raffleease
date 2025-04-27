package com.raffleease.raffleease.Domains.Images.DTOs;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record ImageDTO(
        @NotNull(message = "Image ID cannot be null")
        Long id,

        @NotBlank(message = "File name cannot be blank")
        String fileName,

        @NotBlank(message = "File path cannot be blank")
        String filePath,

        @NotBlank(message = "Content type cannot be blank")
        String contentType,

        @NotBlank(message = "URL cannot be blank")
        String url,

        @NotNull(message = "Image order cannot be null")
        @Min(value = 1, message = "Image order must be a positive number")
        @Max(value = 10, message = "Image order cannot be greater than 10")
        Integer imageOrder
) { }