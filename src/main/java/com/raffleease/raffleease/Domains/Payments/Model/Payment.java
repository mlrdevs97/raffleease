package com.raffleease.raffleease.Domains.Payments.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "Payments")
public class Payment {
    @Id
    private Long id;
    private Long orderId;
    private PaymentStatus status;
    private String paymentMethod;
    private BigDecimal total;
    private String stripePaymentId;
}