package com.raffleease.raffleease.Domains.Images.Services;

import com.raffleease.raffleease.Domains.Images.DTOs.UpdateOrderRequest;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface ImagesUpdateOrderService {
    ImageResponse updateImageOrderOnCreate(HttpServletRequest request, UpdateOrderRequest updateOrderRequest);
    ImageResponse updateImageOrderOnEdit(HttpServletRequest request, Long raffleId, UpdateOrderRequest updateOrderRequest);
}
