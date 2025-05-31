package com.raffleease.raffleease.Domains.Payments.Services;

import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;

import java.math.BigDecimal;

public interface PaymentsService {
    Payment create(Order order, BigDecimal total);
}