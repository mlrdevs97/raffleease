package com.raffleease.raffleease.Domains.Orders.Services;

import com.raffleease.raffleease.Domains.Orders.Model.Order;

public interface OrdersPersistenceService {
    Order findById(Long id);
    Order save(Order entity);
}