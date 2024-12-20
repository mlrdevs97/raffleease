package com.raffleease.raffleease.Domains.Orders.Services.Impls;

import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Orders.Repository.IOrdersRepository;
import com.raffleease.raffleease.Domains.Orders.Services.IOrdersQueryService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrdersQueryServiceImpl implements IOrdersQueryService {
    private final IOrdersRepository repository;

    public Order findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found for id <" + id + ">"));
    }
}
