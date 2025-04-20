package com.raffleease.raffleease.Domains.Payments.Services;

import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Payments.DTOs.PaymentEdit;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;
import com.raffleease.raffleease.Domains.Payments.Model.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public interface PaymentsService {
    Payment create();
    Payment create(Order order, BigDecimal total);
    Payment edit(Long paymentId, PaymentEdit paymentEdit);
    Payment edit(Payment payment, PaymentEdit paymentEdit);
}