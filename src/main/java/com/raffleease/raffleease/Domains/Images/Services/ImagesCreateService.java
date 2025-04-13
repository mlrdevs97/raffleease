package com.raffleease.raffleease.Domains.Images.Services;

import com.raffleease.raffleease.Domains.Images.DTOs.ImageResponse;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageUpload;

public interface ImagesCreateService {
    ImageResponse create(Long associationId, ImageUpload uploadRequest);
    ImageResponse create(Long associationId, Long raffleId, ImageUpload uploadRequest);
}
