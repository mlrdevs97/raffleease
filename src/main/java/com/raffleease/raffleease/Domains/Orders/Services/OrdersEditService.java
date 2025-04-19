package com.raffleease.raffleease.Domains.Orders.Services;

import com.raffleease.raffleease.Domains.Orders.DTOs.*;
import com.raffleease.raffleease.Domains.Orders.Model.Order;

public interface OrdersEditService {
    Order edit(Order order, OrderEdit orderEdit);
    Object completeOrder(Long orderId, OrderComplete orderComplete);
    OrderDTO cancelOrder(Long orderId);
    OrderDTO addComment(Long orderId, AddCommentRequest request);
}
