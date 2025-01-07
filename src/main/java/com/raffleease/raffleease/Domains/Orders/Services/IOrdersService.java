package com.raffleease.raffleease.Domains.Orders.Services;

import com.raffleease.raffleease.Domains.Orders.DTOs.OrderEdit;
import com.raffleease.raffleease.Domains.Orders.Model.Order;

public interface IOrdersService {
    String create(Long cartId);
    Order edit(Order order, OrderEdit orderEdit);
    Order findById(Long id);
}
