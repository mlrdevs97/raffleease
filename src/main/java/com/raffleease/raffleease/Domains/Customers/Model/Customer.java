package com.raffleease.raffleease.Domains.Customers.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(name = "Customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String stripeId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdAt;
}
