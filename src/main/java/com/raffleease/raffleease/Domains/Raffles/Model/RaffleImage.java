package com.raffleease.raffleease.Domains.Raffles.Model;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "raffle_images")
public class RaffleImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "raffle_id", nullable = false)
    private Raffle raffle;

    @Column(name = "key", nullable = false)
    private String key;
}
