package com.raffleease.raffleease.Domains.Orders.Services;

import com.raffleease.raffleease.Domains.Orders.DTOs.OrderDTO;
import com.raffleease.raffleease.Domains.Orders.DTOs.OrderSearchFilters;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrdersService {
    OrderDTO get(Long id);
    Page<OrderDTO> search(OrderSearchFilters filters, Long associationId, Pageable pageable);
    Order findById(Long id);
    Order save(Order entity);
}