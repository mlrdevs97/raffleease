package com.raffleease.raffleease.Domains.Raffles.Model;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "Raffles")
public class Raffle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private RaffleStatus status;

    private String URL;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @OneToMany(mappedBy = "raffle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RaffleImage> images;

    @Column(nullable = false)
    private BigDecimal ticketPrice;

    private Long firstTicketNumber;

    private BigDecimal revenue;

    private Long totalTickets;

    private Long availableTickets;

    private Long soldTickets;

    @OneToMany(mappedBy = "raffle", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Ticket> tickets;

    @OneToOne()
    private Association association;

}