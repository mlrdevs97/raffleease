package com.raffleease.raffleease.Domains.Orders.Services.Impls;

import com.raffleease.raffleease.Domains.Cart.Model.Cart;
import com.raffleease.raffleease.Domains.Cart.Services.ICartsService;
import com.raffleease.raffleease.Domains.Orders.DTOs.OrderEdit;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Orders.Repository.IOrdersRepository;
import com.raffleease.raffleease.Domains.Orders.Services.IOrdersService;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;
import com.raffleease.raffleease.Domains.Payments.Services.IPaymentsService;
import com.raffleease.raffleease.Domains.Payments.Services.IStripeService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class OrdersServiceImpl implements IOrdersService {
    private final IOrdersRepository repository;
    private final ICartsService cartsService;
    private final IPaymentsService paymentsService;
    private final IStripeService stripeService;

    @Override
    public String create(Long cartId) {
        Cart cart = cartsService.findById(cartId);
        Payment payment = paymentsService.create();

        Order order = save(Order.builder()
                .payment(payment)
                .cart(cart)
                .build()
        );

        return stripeService.createSession(order);
    }

    @Override
    public Order edit(Order order, OrderEdit orderEdit) {
        if (Objects.nonNull(orderEdit.cart())) {
            order.setCart(orderEdit.cart());
        }

        if (Objects.nonNull(orderEdit.payment())) {
            order.setPayment(orderEdit.payment());
        }

        if (Objects.nonNull(orderEdit.customer())) {
            order.setCustomer(orderEdit.customer());
        }

        if (Objects.nonNull(orderEdit.orderDate())) {
            order.setOrderDate(orderEdit.orderDate());
        }

        return save(order);
    }

    @Override
    public Order findById(Long id) {
        try {
            return repository.findById(id).orElseThrow(() -> new NotFoundException("Order not found for id <" + id + ">"));
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while fetching order with ID <" + id + ">: " + ex.getMessage());
        }
    }

    private Order save(Order entity) {
        try {
            return repository.save(entity);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving order: " + ex.getMessage());
        }
    }
}
