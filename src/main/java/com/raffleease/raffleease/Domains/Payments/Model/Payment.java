package com.raffleease.raffleease.Domains.Payments.Model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "Payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String paymentIntentId;
    private PaymentStatus status;
    private String paymentMethod;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}