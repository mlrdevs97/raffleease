package com.raffleease.raffleease.Domains.Raffles.Model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import static jakarta.persistence.FetchType.LAZY;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class RaffleStatistics {
    @Id
    private Long id;

    @OneToOne(fetch = LAZY)
    @MapsId
    @JoinColumn(name = "raffle_id")
    private Raffle raffle;

    @Column(nullable = false)
    private Long availableTickets;

    @Column(nullable = false)
    private Long soldTickets;

    @Column(nullable = false)
    private Long closedSells;

    @Column(nullable = false)
    private Long failedSells;

    @Column(nullable = false)
    private Long refundTickets;

    @Column(nullable = false)
    private Long unpaidTickets;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal revenue;
}
