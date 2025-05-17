package com.raffleease.raffleease.Domains.Payments.Services.Impls;

import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Payments.DTOs.PaymentEdit;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;
import com.raffleease.raffleease.Domains.Payments.Repository.PaymentsRepository;
import com.raffleease.raffleease.Domains.Payments.Services.PaymentsService;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

import static com.raffleease.raffleease.Domains.Payments.Model.PaymentStatus.PENDING;

@RequiredArgsConstructor
@Service
public class PaymentsServiceImpl implements PaymentsService {
    private final PaymentsRepository repository;

    @Override
    public Payment create() {
        Payment payment = Payment.builder()
                .status(PENDING)
                .build();
        return save(payment);
    }

    @Override
    public Payment create(Order order, BigDecimal total) {
        Payment payment = Payment.builder()
                .order(order)
                .status(PENDING)
                .total(total)
                .build();
        return save(payment);
    }

    @Override
    public Payment edit(Long paymentId, PaymentEdit paymentEdit) {
        Payment payment = findById(paymentId);
        return editInternal(payment, paymentEdit);
    }

    @Override
    public Payment edit(Payment payment, PaymentEdit paymentEdit) {
        return editInternal(payment, paymentEdit);
    }

    private Payment editInternal(Payment payment, PaymentEdit paymentEdit) {
        if (Objects.nonNull(paymentEdit.paymentIntentId())) {
            payment.setPaymentIntentId(paymentEdit.paymentIntentId());
        }

        /*
        if (Objects.nonNull(paymentEdit.paymentMethod())) {
            payment.setPaymentMethod(paymentEdit.paymentMethod());
        }

         */

        if (Objects.nonNull(paymentEdit.paymentStatus())) {
            payment.setStatus(paymentEdit.paymentStatus());
        }

        if (Objects.nonNull(paymentEdit.completedAt())) {
            payment.setUpdatedAt(paymentEdit.completedAt());
        }

        if (Objects.nonNull(paymentEdit.total())) {
            payment.setTotal(paymentEdit.total());
        }

        return save(payment);
    }

    public Payment findById(Long id) {
        try {
            return repository.findById(id).orElseThrow(() -> new NotFoundException("Payment not found for id <" + id + ">"));
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while fetching payment with ID <" + id + ">: " + ex.getMessage());
        }
    }

    private Payment save(Payment payment) {
        try {
            return repository.save(payment);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving payment: " + ex.getMessage());
        }
    }
}