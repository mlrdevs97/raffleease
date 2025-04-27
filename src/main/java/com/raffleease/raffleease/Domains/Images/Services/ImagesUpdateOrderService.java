package com.raffleease.raffleease.Domains.Images.Services;

import com.raffleease.raffleease.Domains.Images.DTOs.UpdateOrderRequest;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageResponse;

public interface ImagesUpdateOrderService {
    ImageResponse updateImageOrderOnEdit(Long associationId, Long raffleId, UpdateOrderRequest updateOrderRequest);
}
