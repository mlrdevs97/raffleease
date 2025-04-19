package com.raffleease.raffleease.Domains.Orders.Services;

import com.raffleease.raffleease.Domains.Orders.DTOs.OrderDTO;
import com.raffleease.raffleease.Domains.Orders.Model.Order;

public interface OrdersService {
    OrderDTO get(Long id);
    Order findById(Long id);
    Order save(Order entity);
}