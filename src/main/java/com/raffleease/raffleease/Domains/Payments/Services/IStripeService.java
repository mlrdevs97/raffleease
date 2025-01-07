package com.raffleease.raffleease.Domains.Payments.Services;

import com.raffleease.raffleease.Domains.Orders.Model.Order;

public interface IStripeService {
    String getPublicKey();
    String createSession(Order order);
}
