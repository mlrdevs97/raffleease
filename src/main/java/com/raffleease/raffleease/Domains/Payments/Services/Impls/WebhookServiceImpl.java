package com.raffleease.raffleease.Domains.Payments.Services.Impls;

import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.Services.CartsService;
import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Customers.Services.CustomersService;
import com.raffleease.raffleease.Domains.Notifications.Services.EmailsService;
import com.raffleease.raffleease.Domains.Orders.DTOs.OrderEdit;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Orders.Services.OrdersEditService;
import com.raffleease.raffleease.Domains.Orders.Services.OrdersService;
import com.raffleease.raffleease.Domains.Payments.DTOs.PaymentEdit;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;
import com.raffleease.raffleease.Domains.Payments.Model.PaymentStatus;
import com.raffleease.raffleease.Domains.Payments.Services.PaymentsService;
import com.raffleease.raffleease.Domains.Payments.Services.IWebhookService;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesEditService;
import com.raffleease.raffleease.Domains.Carts.Services.ReservationsService;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Services.TicketsService;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.CustomStripeException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.DeserializationException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.InvalidSignatureException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.StripeWebHookException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.raffleease.raffleease.Domains.Payments.Model.PaymentStatus.*;
import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.SOLD;

@RequiredArgsConstructor
@Service
public class WebhookServiceImpl implements IWebhookService {
    private final PaymentsService paymentsEditService;
    private final CustomersService customersService;
    private final TicketsService ticketsService;
    private final OrdersEditService ordersEditService;
    private final OrdersService ordersService;
    private final RafflesEditService rafflesEditService;
    private final CartsService cartsService;
    private final ReservationsService reservationsService;
    private final EmailsService emailsService;

    @Value("${STRIPE_WEBHOOK_KEY}")
    private String webhookKey;

    @Override
    public void handleWebHook(String payload, String sigHeader) {
        Event event = constructEvent(payload, sigHeader, webhookKey);
        StripeObject stripeObject = deserializeStripeObject(event);
        processPayment(event, stripeObject);
    }

    private Event constructEvent(String payload, String sigHeader, String webhookKey) {
        try {
            return Webhook.constructEvent(payload, sigHeader, webhookKey);
        } catch (SignatureVerificationException exp) {
            throw new InvalidSignatureException("Invalid signature for Stripe webhook provided: " + exp.getMessage());
        } catch (Exception exp) {
            throw new StripeWebHookException("Unexpected error processing Stripe webhook: " + exp.getMessage());
        }
    }

    private StripeObject deserializeStripeObject(Event event) {
        return event.getDataObjectDeserializer().getObject()
                .orElseThrow(() -> new DeserializationException("Failed to deserialize Stripe object from event"));
    }

    private void processPayment(Event event, StripeObject stripeObject) {
        if (stripeObject instanceof PaymentIntent paymentIntent) {
            Order order = getOrder(paymentIntent);
            Payment payment = updatePayment(order.getPayment(), paymentIntent);

            // TODO: Check and fix close Cart
            /*
            Cart cart = cartsService.edit(order.getCart(), CLOSED);
             */

            Customer customer = createCustomer(paymentIntent);

            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentSuccess(order);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentFailure(order);
                    break;
                case "payment_intent.canceled":
                    handlePaymentCanceled(order);
                    break;
            }

            // TODO: Check and fix this
            Cart cart = new Cart();
            updateOrder(order, payment, customer, cart);
        }
    }

    private void handlePaymentSuccess(Order order) {
        paymentsEditService.edit(order.getPayment(), PaymentEdit.builder()
                .paymentStatus(SUCCEEDED)
                .completedAt(LocalDateTime.now())
                .build()
        );

        // TODO: Get tickets and mark as sold
        List<Ticket> tickets = new ArrayList<>();
        List<Ticket> purchasedTickets = ticketsService.updateStatus(tickets, SOLD);

        // TODO: Update statistics
        /*
        rafflesEditService.updateStatistics(
                order.getCart().getRaffle(),
                Optional.ofNullable(order.getPayment().getTotal()).orElse(ZERO),
                (long) purchasedTickets.size()
        );

         */
        emailsService.sendOrderSuccessEmail(order);
    }

    private void handlePaymentFailure(Order order) {
        handlePaymentUnsuccessful(order, FAILED);
    }

    private void handlePaymentCanceled(Order order) {
        handlePaymentUnsuccessful(order, CANCELLED);
    }

    private void handlePaymentUnsuccessful(Order order, PaymentStatus paymentStatus) {
        paymentsEditService.edit(order.getPayment(), PaymentEdit.builder()
                .paymentStatus(paymentStatus)
                .completedAt(LocalDateTime.now())
                .build()
        );

        // TODO: Release tickets
        /*
        reservationsReleaseService.release(order.getCart());
         */
    }

    private Payment updatePayment(Payment payment, PaymentIntent paymentIntent) {
        String paymentMethod = retrieveStripePaymentMethod(paymentIntent.getPaymentMethod()).getType();
        return paymentsEditService.edit(
                payment,
                PaymentEdit.builder()
                        .completedAt(LocalDateTime.now())
                        .total(BigDecimal.valueOf(paymentIntent.getAmount()))
                        .paymentMethod(paymentMethod)
                        .paymentIntentId(paymentIntent.getId())
                        .build()
        );
    }

    private Customer createCustomer(PaymentIntent paymentIntent) {
        com.stripe.model.Customer customerData = retrieveStripeCustomerData(paymentIntent);
        return customersService.create(
                customerData.getId(),
                customerData.getName(),
                customerData.getEmail(),
                customerData.getPhone()
        );
    }

    private PaymentMethod retrieveStripePaymentMethod(String paymentMethodId) {
        try {
            return PaymentMethod.retrieve(paymentMethodId);
        } catch (StripeException exp) {
            throw new CustomStripeException("Error retrieving payment data from stripe: " + exp.getMessage());
        }
    }

    private com.stripe.model.Customer retrieveStripeCustomerData(PaymentIntent paymentIntent) {
        try {
            return com.stripe.model.Customer.retrieve(paymentIntent.getCustomer());
        } catch (StripeException exp) {
            throw new CustomStripeException("Error retrieving customer data from stripe: " + exp.getMessage());
        }
    }

    private Order getOrder(PaymentIntent paymentIntent) {
        Long orderId = Long.parseLong(paymentIntent.getMetadata().get("orderId"));
        return ordersService.findById(orderId);
    }

    private void updateOrder(Order order, Payment payment, Customer customer, Cart cart) {
        ordersEditService.edit(order, OrderEdit.builder()
                .customer(customer)
                .payment(payment)
                .cart(cart)
                .orderDate(LocalDateTime.now())
                .build()
        );
    }
}