package com.raffleease.raffleease.Domains.Images.Services;

import com.raffleease.raffleease.Domains.Images.DTOs.ImageResponse;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageUpload;
import jakarta.servlet.http.HttpServletRequest;

public interface ImagesCreateService {
    ImageResponse create(HttpServletRequest request, ImageUpload uploadRequest);
    ImageResponse create(HttpServletRequest request, Long raffleId, ImageUpload uploadRequest);
}
