package com.raffleease.raffleease.Domains.Orders.Services;

import com.raffleease.raffleease.Domains.Orders.DTOs.*;

public interface OrdersEditService {
    Object completeOrder(Long orderId, OrderComplete orderComplete);
    OrderDTO cancelOrder(Long orderId);
    OrderDTO addComment(Long orderId, CommentRequest request);
}
