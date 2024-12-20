package com.raffleease.raffleease.Domains.Payments.Services.Impls;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleDTO;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesQueryService;
import com.raffleease.raffleease.Domains.Payments.DTOs.SessionCreate;
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
    private final IRafflesQueryService rafflesQueryService;

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
    public String createSession(SessionCreate request) {
        Stripe.apiKey = stripeSecretKey;
        RaffleDTO raffle = getRaffleInfo(request.raffleId());
        SessionCreateParams params = buildSessionParams(request, raffle);
        Session session = buildSession(params);
        return session.getClientSecret();
    }

    private SessionCreateParams buildSessionParams(SessionCreate request, RaffleDTO raffle) {
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
                                .putMetadata("orderId", request.orderId().toString())
                                .putMetadata("raffleId", request.raffleId().toString())
                                .build()
                )
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Ticket for raffle " + raffle.title())
                                                                .build()
                                                )
                                                .setUnitAmount(raffle.ticketPrice().multiply(BigDecimal.valueOf(100)).longValue())
                                                .build()
                                )
                                .setQuantity(request.quantity())
                                .build()
                )
                .setReturnUrl(clientHost + clientPath + raffle.id())
                .build();
    }

    private Session buildSession(SessionCreateParams params) {
        try {
            return Session.create(params);
        } catch (StripeException ex) {
            throw new CustomStripeException("Error creating checkout session: " + ex.getMessage());
        }
    }

    private RaffleDTO getRaffleInfo(Long raffleId) {
        return rafflesQueryService.get(raffleId);
    }
}