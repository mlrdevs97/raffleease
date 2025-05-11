package com.raffleease.raffleease.Domains.Payments.Services.Impls;

import com.raffleease.raffleease.Configs.CorsProperties;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Payments.Services.StripeService;
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
public class StripeServiceImpl implements StripeService {
    private final CorsProperties corsProperties;

    @Value("${spring.stripe.keys.public}")
    private String stripePublicKey;

    @Value("${spring.stripe.keys.secret}")
    private String stripeSecretKey;

    @Value("${spring.application.paths.client.payment_success}")
    private String paymentSuccessPath;

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
        // TODO: CHeck how to get raffle
        Raffle raffle = new Raffle();
        /*
        Raffle raffle = order.getCart().getRaffle();

         */

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
                                // Check and fix how to set quantity
                                /*
                                .setQuantity((long) order.getCart().getTickets().size())
                                 */
                                .build()
                )
                .setReturnUrl(corsProperties.getClientAsList().get(0) + paymentSuccessPath + raffle.getId())
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