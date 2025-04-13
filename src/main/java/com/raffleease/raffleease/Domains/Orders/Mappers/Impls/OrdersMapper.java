package com.raffleease.raffleease.Domains.Orders.Mappers.Impls;

import com.raffleease.raffleease.Domains.Customers.Mappers.ICustomersMapper;
import com.raffleease.raffleease.Domains.Orders.DTOs.OrderDTO;
import com.raffleease.raffleease.Domains.Orders.DTOs.OrderItemDTO;
import com.raffleease.raffleease.Domains.Orders.Mappers.IOrdersMapper;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Orders.Model.OrderItem;
import com.raffleease.raffleease.Domains.Payments.Mappers.IPaymentsMapper;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrdersMapper implements IOrdersMapper {
    private final ICustomersMapper customersMapper;
    private final IPaymentsMapper paymentsMapper;

    public OrderDTO fromOrder(Order order, List<Ticket> purchasedTickets) {
        return OrderDTO.builder()
                .id(order.getId())
                .orderReference(order.getOrderReference())
                .orderDate(order.getCreatedAt())
                .orderItems(fromOrderItemList(order.getOrderItems()))
                .customer(customersMapper.fromCustomer(order.getCustomer()))
                .payment(paymentsMapper.fromPayment(order.getPayment()))
                .build();
    }

    private List<OrderItemDTO> fromOrderItemList(List<OrderItem> orderItems) {
        return orderItems.stream().map(orderItem -> OrderItemDTO.builder()
                .id(orderItem.getId())
                .priceAtPurchase(orderItem.getPriceAtPurchase())
                .ticketNumber(orderItem.getTicketNumber())
                .raffleId(orderItem.getRaffleId())
                .ticketId(orderItem.getTicketId())
                .build()
        ).toList();
    }
}
