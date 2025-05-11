package com.raffleease.raffleease.Domains.Orders.Services.Impls;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.Services.CartsService;
import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Customers.Services.CustomersService;
import com.raffleease.raffleease.Domains.Orders.DTOs.AdminOrderCreate;
import com.raffleease.raffleease.Domains.Orders.DTOs.OrderDTO;
import com.raffleease.raffleease.Domains.Orders.Mappers.OrdersMapper;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Orders.Model.OrderItem;
import com.raffleease.raffleease.Domains.Orders.Services.OrdersCreateService;
import com.raffleease.raffleease.Domains.Orders.Services.OrdersService;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;
import com.raffleease.raffleease.Domains.Payments.Services.PaymentsService;
import com.raffleease.raffleease.Domains.Payments.Services.StripeService;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesQueryService;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Services.TicketsQueryService;
import com.raffleease.raffleease.Domains.Tickets.Services.TicketsService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.raffleease.raffleease.Domains.Carts.Model.CartStatus.CLOSED;
import static com.raffleease.raffleease.Domains.Orders.Model.OrderSource.ADMIN;
import static com.raffleease.raffleease.Domains.Orders.Model.OrderStatus.PENDING;
import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;
import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

@RequiredArgsConstructor
@Service
public class OrdersCreateServiceImpl implements OrdersCreateService {
    private final OrdersService ordersService;
    private final CartsService cartsService;
    private final RafflesQueryService rafflesQueryService;
    private final CustomersService customersService;
    private final TicketsQueryService ticketsQueryService;
    private final TicketsService ticketsService;
    private final PaymentsService paymentsService;
    private final StripeService stripeService;
    private final AssociationsService associationsService;
    private final OrdersMapper mapper;

    @Override
    @Transactional
    public OrderDTO create(AdminOrderCreate adminOrder, Long associationId) {
        Association association = associationsService.findById(associationId);
        Cart cart = cartsService.findById(adminOrder.cartId());
        List<Ticket> requestedTickets = ticketsQueryService.findAllById(adminOrder.ticketIds());
        validateRequest(requestedTickets, cart, association);
        closeCart(cart);
        Customer customer = customersService.create(adminOrder.customer());
        finalizeTickets(requestedTickets, customer);
        Order order = createOrder(association, customer, adminOrder.comment());
        Payment payment = createPayment(order, requestedTickets);
        List<OrderItem> orderItems = createOrderItems(order, requestedTickets);
        order.setPayment(payment);
        order.getOrderItems().addAll(orderItems);
        order = ordersService.save(order);
        return mapper.fromOrder(order);
    }

    @Override
    public String create(Long cartId) {
        Cart cart = cartsService.findById(cartId);
        Payment payment = paymentsService.create();

        // TODO: Check and fix order items
        Order order = ordersService.save(Order.builder()
                .payment(payment)
                .orderItems(new ArrayList<>())
                .build()
        );

        return stripeService.createSession(order);
    }

    private void validateRequest(List<Ticket> requestedTickets, Cart cart, Association association) {
        List<Ticket> cartTickets = ticketsQueryService.findAllByCart(cart);
        validateAllTicketsBelongToAssociationRaffle(requestedTickets, association);
        validateAllTicketsBelongToCart(cartTickets, requestedTickets);
        validateAllCartTicketsIncludedInRequest(cartTickets, requestedTickets);
    }

    private void closeCart(Cart cart) {
        cart.setStatus(CLOSED);
        cart.setTickets(null);
        cartsService.save(cart);
    }

    private void finalizeTickets(List<Ticket> tickets, Customer customer) {
        ticketsService.saveAll(tickets.stream().peek(ticket -> {
            ticket.setCustomer(customer);
            ticket.setCart(null);
        }).toList());
    }

    private Payment createPayment(Order order, List<Ticket> tickets) {
        BigDecimal total = calculateOrderTotal(tickets);
        return paymentsService.create(order, total);
    }

    private Order createOrder(Association association, Customer customer, String comment) {
        return ordersService.save(Order.builder()
                .association(association)
                .status(PENDING)
                .orderSource(ADMIN)
                .orderReference(generateOrderReference())
                .customer(customer)
                .orderItems(new ArrayList<>())
                .comment(comment)
                .build());
    }

    private List<OrderItem> createOrderItems(Order order, List<Ticket> tickets) {
        return tickets.stream().map(ticket -> OrderItem.builder()
                .order(order)
                .ticketNumber(ticket.getTicketNumber())
                .priceAtPurchase(ticket.getRaffle().getTicketPrice())
                .ticketId(ticket.getId())
                .raffleId(ticket.getRaffle().getId())
                .build()).toList();
    }

    private void validateAllTicketsBelongToCart(List<Ticket> cartTickets, List<Ticket> requestedTickets) {
        Set<Long> cartTicketIds = cartTickets.stream()
                .map(Ticket::getId)
                .collect(Collectors.toSet());

        boolean anyNotBelong = requestedTickets.stream()
                .map(Ticket::getId)
                .anyMatch(id -> !cartTicketIds.contains(id));

        if (anyNotBelong) {
            throw new BusinessException("Some tickets do not belong to current cart");
        }
    }

    private void validateAllCartTicketsIncludedInRequest(List<Ticket> cartTickets, List<Ticket> requestedTickets) {
        Set<Long> requestedTicketIds = requestedTickets.stream()
                .map(Ticket::getId)
                .collect(Collectors.toSet());

        boolean anyNotIncluded = cartTickets.stream()
                .map(Ticket::getId)
                .anyMatch(id -> !requestedTicketIds.contains(id));

        if (anyNotIncluded) {
            throw new BusinessException("Some tickets in cart are not included in order request");
        }
    }

    private void validateAllTicketsBelongToAssociationRaffle(List<Ticket> tickets, Association association) {
        Set<Raffle> associationRaffles = new HashSet<>(rafflesQueryService.findAllByAssociation(association));
        Set<Raffle> ticketsRaffles = tickets.stream().map(Ticket::getRaffle).collect(Collectors.toSet());

        boolean anyNotBelong = ticketsRaffles.stream().anyMatch(raffle -> !associationRaffles.contains(raffle));
        if (anyNotBelong) {
            throw new BusinessException("Some tickets do not belong to an association raffle");
        }
    }

    /**
     * Returns the total price of the given tickets, based on their raffle's ticket price.
     * Groups tickets by raffle and sums (ticket price × quantity) for each group.
     *
     * @param tickets the list of tickets to calculate the total for
     * @return the total price as a {@link BigDecimal}
     */
    private BigDecimal calculateOrderTotal(List<Ticket> tickets) {
        return  tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getRaffle, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> entry.getKey().getTicketPrice().multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateOrderReference() {
        String datePart = LocalDateTime.now().format(BASIC_ISO_DATE);
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + datePart + "-" + randomPart;
    }
}
