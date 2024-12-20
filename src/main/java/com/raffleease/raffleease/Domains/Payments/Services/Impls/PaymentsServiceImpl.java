package com.raffleease.raffleease.Domains.Payments.Services.Impls;

import com.raffleease.raffleease.Domains.Orders.Services.IOrderResultsService;
import com.raffleease.raffleease.Domains.Payments.DTOs.PaymentDTO;
import com.raffleease.raffleease.Domains.Payments.Mappers.PaymentsMapper;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;
import com.raffleease.raffleease.Domains.Payments.Repository.IPaymentsRepository;
import com.raffleease.raffleease.Domains.Payments.Services.IPaymentsService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class PaymentsService implements IPaymentsService {
    private final IPaymentsRepository repository;
    private final IOrderResultsService orderResultsService;
    private final PaymentsMapper mapper;

    @Override
    public PaymentDTO createPayment(
            Long orderId,
            String paymentMethod,
            BigDecimal total,
            String paymentIntentId
    ) {
        Payment payment = buildPayment(orderId, paymentMethod, total, paymentIntentId);
        Payment savedPayment = savePayment(payment);
        return mapper.fromPayment(savedPayment);
    }

    public Payment savePayment(Payment payment) {
        try {
            return repository.save(payment);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while creating new payment: " + ex.getMessage());
        }
    }

    private Payment buildPayment(
            Long orderId,
            String paymentMethod,
            BigDecimal total,
            String paymentIntentId
    ) {
        return Payment.builder()
                .orderId(orderId)
                .paymentMethod(paymentMethod)
                .total(total)
                .stripePaymentId(paymentIntentId)
                .build();
    }
}