package com.raffleease.raffleease.Domains.Orders.Services;

import com.raffleease.raffleease.Domains.Orders.DTOs.*;

public interface OrdersEditService {
    OrderDTO complete(Long orderId, OrderComplete orderComplete);
    OrderDTO cancel(Long orderId);
    OrderDTO refund(Long orderId);
    OrderDTO setUnpaid(Long orderId);
    OrderDTO addComment(Long orderId, CommentRequest request);
}
