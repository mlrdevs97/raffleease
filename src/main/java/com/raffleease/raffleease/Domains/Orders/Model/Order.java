package com.raffleease.raffleease.Domains.Orders.Model;

import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Entity
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderReference;

    @OneToMany(mappedBy = "order", cascade = ALL)
    private List<OrderItem> orderItems;

    @OneToOne(cascade = ALL, orphanRemoval = true)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @OneToOne(cascade = ALL, orphanRemoval = true)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}