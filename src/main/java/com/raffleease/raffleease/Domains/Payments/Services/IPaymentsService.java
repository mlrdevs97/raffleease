package com.raffleease.raffleease.Domains.Payments.Services;

import com.raffleease.raffleease.Domains.Payments.DTOs.PaymentEdit;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;

public interface IPaymentsService {
    Payment create();
    Payment edit(Long paymentId, PaymentEdit paymentEdit);
    Payment edit(Payment payment, PaymentEdit paymentEdit);
}
