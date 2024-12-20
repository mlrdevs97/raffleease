package com.raffleease.raffleease.Domains.Orders.Services;

public interface IOrderResultsService {
    void handleOrderSuccess(PaymentSuccess request);
    void handleOrderFailure(PaymentFailure request);
}
