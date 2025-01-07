package com.raffleease.raffleease.Domains.Notifications.Model;

import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private NotificationType notificationType;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime notificationDate;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}