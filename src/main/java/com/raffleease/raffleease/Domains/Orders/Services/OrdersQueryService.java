package com.raffleease.raffleease.Domains.Orders.Services;

import com.raffleease.raffleease.Domains.Orders.DTOs.OrderDTO;
import com.raffleease.raffleease.Domains.Orders.DTOs.OrderSearchFilters;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrdersQueryService {
    OrderDTO get(Long id);
    Page<OrderDTO> search(OrderSearchFilters filters, Long associationId, Pageable pageable);
}
