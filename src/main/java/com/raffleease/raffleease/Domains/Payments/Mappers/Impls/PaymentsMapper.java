package com.raffleease.raffleease.Domains.Payments.Mappers.Impls;

import com.raffleease.raffleease.Domains.Payments.DTOs.PaymentDTO;
import com.raffleease.raffleease.Domains.Payments.Mappers.IPaymentsMapper;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;
import org.springframework.stereotype.Service;

@Service
public class PaymentsMapper implements IPaymentsMapper {
    public PaymentDTO fromPayment(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .paymentIntentId(payment.getPaymentIntentId())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .total(payment.getTotal())
                .build();
    }
}