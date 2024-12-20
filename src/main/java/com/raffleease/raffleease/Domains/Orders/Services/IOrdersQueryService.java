package com.raffleease.raffleease.Domains.Orders.Services;

import com.raffleease.raffleease.Domains.Orders.Model.Order;

public interface IOrdersQueryService {
    Order findById(Long id);
}
