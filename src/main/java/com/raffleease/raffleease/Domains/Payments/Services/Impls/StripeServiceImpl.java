package com.raffleease.raffleease.Domains.Payments.Services.Impls;

import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Payments.Services.IStripeService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.CustomStripeException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class StripeServiceImpl implements IStripeService {
    @Value("${STRIPE_PUBLIC_KEY}")
    private String stripePublicKey;

    @Value("${STRIPE_SECRET_KEY}")
    private String stripeSecretKey;

    @Value("${CLIENT_HOST}")
    private String clientHost;

    @Value("${CLIENT_PATH}")
    private String clientPath;

    @Override
    public String getPublicKey() {
        return stripePublicKey;
    }

    @Override
    public String createSession(Order order) {
        Stripe.apiKey = stripeSecretKey;
        SessionCreateParams params = buildSessionParams(order);
        Session session = buildSession(params);
        return session.getClientSecret();
    }

    private SessionCreateParams buildSessionParams(Order order) {
        Raffle raffle = order.getCart().getRaffle();

        return SessionCreateParams.builder()
                .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setPhoneNumberCollection(
                        SessionCreateParams.PhoneNumberCollection.builder().setEnabled(true).build()
                )
                .setCustomerCreation(
                        SessionCreateParams.CustomerCreation.ALWAYS
                )
                .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                                .putMetadata("orderId", order.getId().toString())
                                .build()
                )
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Ticket for raffle " + raffle.getTitle())
                                                                .build()
                                                )
                                                .setUnitAmount(raffle.getTicketPrice().multiply(BigDecimal.valueOf(100)).longValue())
                                                .build()
                                )
                                .setQuantity((long) order.getCart().getTickets().size())
                                .build()
                )
                .setReturnUrl(clientHost + clientPath + raffle.getId())
                .build();
    }

    private Session buildSession(SessionCreateParams params) {
        try {
            return Session.create(params);
        } catch (StripeException ex) {
            throw new CustomStripeException("Error creating checkout session: " + ex.getMessage());
        }
    }
}