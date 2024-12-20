package com.raffleease.raffleease.Domains.Payments.Mappers;

import com.raffleease.raffleease.Domains.Payments.DTOs.PaymentDTO;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;
import org.springframework.stereotype.Service;

@Service
public class PaymentsMapper {
    public PaymentDTO fromPayment(Payment payment) {
        return PaymentDTO.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .paymentIntentId(payment.getStripePaymentId())
                .build();
    }
}