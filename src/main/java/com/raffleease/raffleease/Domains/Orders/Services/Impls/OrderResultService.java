package com.raffleease.raffleease.Domains.Orders.Services.Impls;

import com.raffleease.raffleease.Domains.Orders.DTOs.OrderDTO;
import com.raffleease.raffleease.Domains.Orders.Mappers.OrdersMapper;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Orders.Repository.IOrdersRepository;
import com.raffleease.raffleease.Domains.Orders.Services.IOrderResultsService;
import com.raffleease.raffleease.Domains.Tickets.DTO.ReservationRequest;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketDTO;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketReservationsService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class OrderResultService implements IOrderResultsService {
    private final IOrdersRepository repository;
    private final OrdersMapper mapper;
    private final ITicketReservationsService ticketReservationsService;

    @Transactional
    @Override
    public void handleOrderSuccess(
            PaymentSuccess request
    ) {
        Order order = findById(request.orderId());
        updateStatus(order, COMPLETED);
        Set<TicketDTO> purchasedTickets = purchaseTickets(
                order.getTicketsIds(),
                request.customer().stripeId()
        );
        OrderDTO orderData = mapper.fromOrder(order, purchasedTickets);
        updateRaffleStatistics(
                request.payment().total(),
                (long) purchasedTickets.size()
        );
        notifyPaymentSuccess(
                request.payment(),
                request.customer(),
                orderData
        );
    }

    @Transactional
    @Override
    public void handleOrderFailure(PaymentFailure request) {
        Order order = findById(request.orderId());
        updateStatus(order, request.status());
        releaseTickets(order.getTicketsIds(), request.raffleId());
    }

    private Set<TicketDTO> purchaseTickets(
            Set<String> ticketsIds,
            String customerId
    ) {
        return ticketsClient.purchase(
                PurchaseRequest.builder()
                        .customerId(customerId)
                        .ticketsIds(ticketsIds)
                        .build()
        );
    }

    private void updateRaffleStatistics(BigDecimal total, Long quantity) {
        statisticsProducer.updateStatistics(
                PurchaseStatistics.builder()
                        .total(total)
                        .quantity(quantity)
                        .build()
        );
    }

    private void releaseTickets(Set<Long> ticketsIds, Long raffleId) {
        ticketReservationsService.release(
                ReservationRequest.builder()
                        .ticketsIds(ticketsIds)
                        .raffleId(raffleId)
                        .build()
        );
    }
}