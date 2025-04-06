package com.raffleease.raffleease.Domains.Orders.Model;

import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import static jakarta.persistence.GenerationType.IDENTITY;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String ticketNumber;
    private BigDecimal priceAtPurchase;
    private Long ticketId;
    private Long raffleId;

    @OneToMany
    @JoinColumn(name = "order_id")
    private Order order;
}
