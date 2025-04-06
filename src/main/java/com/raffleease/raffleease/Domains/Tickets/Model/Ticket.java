package com.raffleease.raffleease.Domains.Tickets.Model;

import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Entity
@Table(name = "Tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ticketNumber;
    private TicketStatus status;

    @ManyToOne
    @JoinColumn(name = "raffle_id", nullable = false)
    private Raffle raffle;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
}