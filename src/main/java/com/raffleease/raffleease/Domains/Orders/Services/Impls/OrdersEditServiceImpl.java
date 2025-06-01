package com.raffleease.raffleease.Domains.Orders.Services.Impls;

import com.raffleease.raffleease.Domains.Carts.Services.ReservationsService;
import com.raffleease.raffleease.Domains.Orders.DTOs.*;
import com.raffleease.raffleease.Domains.Orders.Mappers.OrdersMapper;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Orders.Model.OrderItem;
import com.raffleease.raffleease.Domains.Orders.Model.OrderStatus;
import com.raffleease.raffleease.Domains.Orders.Services.OrdersService;
import com.raffleease.raffleease.Domains.Orders.Services.OrdersEditService;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesService;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Services.TicketsQueryService;
import com.raffleease.raffleease.Domains.Tickets.Services.TicketsService;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.raffleease.raffleease.Domains.Orders.Model.OrderStatus.*;
import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.SOLD;

@RequiredArgsConstructor
@Service
public class OrdersEditServiceImpl implements OrdersEditService {
    private final OrdersService ordersService;
    private final ReservationsService reservationsService;
    private final TicketsService ticketsService;
    private final TicketsQueryService ticketsQueryService;
    private final OrdersMapper mapper;
    private final RafflesService rafflesService;

    @Override
    @Transactional
    public OrderDTO complete(Long orderId, OrderComplete orderComplete) {
        Order order = ordersService.findById(orderId);
        throwIfInvalidTransition(order.getStatus(), PENDING, COMPLETED);
        updateTicketsStatus(order);
        rafflesService.completeRaffleIfAllTicketsSold(order.getRaffle());
        LocalDateTime now = LocalDateTime.now();
        Payment payment = order.getPayment();
        payment.setPaymentMethod(orderComplete.paymentMethod());
        payment.setCompletedAt(now);
        order.setStatus(COMPLETED);
        order.setCompletedAt(now);
        return mapper.fromOrder(ordersService.save(order));
    }

    @Override
    @Transactional
    public OrderDTO cancel(Long orderId) {
        Order order = ordersService.findById(orderId);
        throwIfInvalidTransition(order.getStatus(), PENDING, CANCELLED);
        releaseOrderTickets(order);
        order.setStatus(CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        return mapper.fromOrder(ordersService.save(order));
    }

    @Override
    public OrderDTO refund(Long orderId) {
        Order order = ordersService.findById(orderId);
        throwIfInvalidTransition(order.getStatus(), COMPLETED, REFUNDED);
        releaseOrderTickets(order);
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(REFUNDED);
        order.setCompletedAt(now);
        order.setRefundedAt(now);
        return mapper.fromOrder(ordersService.save(order));
    }

    @Override
    public OrderDTO addComment(Long orderId, CommentRequest request) {
        Order order = ordersService.findById(orderId);
        order.setComment(request.comment());
        return mapper.fromOrder(ordersService.save(order));
    }

    private void throwIfInvalidTransition(OrderStatus oldStatus, OrderStatus expectedStatus, OrderStatus newStatus) {
        if (!oldStatus.equals(expectedStatus)) {
            throw new BusinessException(String.format("Unsupported status transition from %s to %s", oldStatus, newStatus));
        }
    }

    private void releaseOrderTickets(Order order) {
        List<Ticket> tickets = getTicketsFromOrder(order);
        reservationsService.release(tickets);
    }

    private void updateTicketsStatus(Order order) {
        List<Ticket> tickets = getTicketsFromOrder(order);
        ticketsService.saveAll(tickets.stream().peek(ticket -> ticket.setStatus(SOLD)).toList());
    }

    private List<Ticket> getTicketsFromOrder(Order order) {
        List<Long> ticketIds = order.getOrderItems().stream().map(OrderItem::getTicketId).toList();
        return ticketsQueryService.findAllById(ticketIds);
    }
}
