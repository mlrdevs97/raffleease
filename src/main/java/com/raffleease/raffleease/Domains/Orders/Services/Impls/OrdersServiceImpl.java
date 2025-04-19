package com.raffleease.raffleease.Domains.Orders.Services.Impls;

import com.raffleease.raffleease.Domains.Orders.DTOs.OrderDTO;
import com.raffleease.raffleease.Domains.Orders.Mappers.OrdersMapper;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Orders.Repository.OrdersRepository;
import com.raffleease.raffleease.Domains.Orders.Services.OrdersService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrdersServiceImpl implements OrdersService {
    private final OrdersRepository repository;
    private final OrdersMapper ordersMapper;

    @Override
    public OrderDTO get(Long id) {
        return ordersMapper.fromOrder(findById(id));
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
    public Order save(Order entity) {
        try {
            return repository.save(entity);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving order: " + ex.getMessage());
        }
    }
}
