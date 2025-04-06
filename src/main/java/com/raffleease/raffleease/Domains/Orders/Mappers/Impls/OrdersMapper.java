package com.raffleease.raffleease.Domains.Orders.Mappers.Impls;

import com.raffleease.raffleease.Domains.Carts.Mappers.ICartsMapper;
import com.raffleease.raffleease.Domains.Customers.Mappers.ICustomersMapper;
import com.raffleease.raffleease.Domains.Orders.DTOs.OrderDTO;
import com.raffleease.raffleease.Domains.Orders.Mappers.IOrdersMapper;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Payments.Mappers.IPaymentsMapper;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrdersMapper implements IOrdersMapper {
    private final ICustomersMapper customersMapper;
    private final ICartsMapper cartsMapper;
    private final IPaymentsMapper paymentsMapper;

    public OrderDTO fromOrder(Order order, List<Ticket> purchasedTickets) {
        return OrderDTO.builder()
                .id(order.getId())
                .orderReference(order.getOrderReference())
                .orderDate(order.getCreatedAt())
                .cart(cartsMapper.fromCart(order.getCart()))
                .customer(customersMapper.fromCustomer(order.getCustomer()))
                .payment(paymentsMapper.fromPayment(order.getPayment()))
                .build();
    }
}
