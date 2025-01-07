package com.raffleease.raffleease.Domains.Images.DTOs;

import lombok.Builder;

@Builder
public record ImageResponse(
        Long id,
        ImageFile imageFile,
        String originalName,
        String filePath
) { }
