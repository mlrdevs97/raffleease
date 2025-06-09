package com.raffleease.raffleease.Domains.Orders.Services.Impls;

import com.raffleease.raffleease.Domains.Orders.DTOs.OrderDTO;
import com.raffleease.raffleease.Domains.Orders.DTOs.OrderSearchFilters;
import com.raffleease.raffleease.Domains.Orders.Mappers.OrdersMapper;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Orders.Repository.OrdersRepository;
import com.raffleease.raffleease.Domains.Orders.Repository.OrdersSearchRepository;
import com.raffleease.raffleease.Domains.Orders.Services.OrdersService;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrdersServiceImpl implements OrdersService {
    private final OrdersSearchRepository searchRepository;
    private final OrdersRepository repository;
    private final OrdersMapper ordersMapper;

    @Override
    public OrderDTO get(Long id) {
        return ordersMapper.fromOrder(findById(id));
    }

    @Override
    public Page<OrderDTO> search(OrderSearchFilters filters, Long associationId, Pageable pageable) {
        Page<Order> ordersPage = searchRepository.searchOrders(filters, associationId, pageable);
        return ordersPage.map(ordersMapper::fromOrder);
    }

    @Override
    public Order findById(Long id) {
        try {
            return repository.findById(id).orElseThrow(() -> new NotFoundException("Order not found for id <" + id + ">"));
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while fetching order with ID <" + id + ">: " + ex.getMessage());
        }
    }

    @Override
    public Order save(Order order) {
        try {
            return repository.save(order);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving order: " + ex.getMessage());
        }
    }

    @Override
    public void deleteComment(Long orderId) {
        Order order = findById(orderId);
        order.setComment(null);
        save(order);
    }
}
