package com.raffleease.raffleease.Domains.Raffles.Model;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    @Column(nullable = false)
    private BigDecimal ticketPrice;

    private Long firstTicketNumber;

    private BigDecimal revenue;

    private Long totalTickets;

    private Long availableTickets;

    private Long soldTickets;

    @OneToMany(mappedBy = "raffle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;

    @OneToMany(mappedBy = "raffle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets;

    @ManyToOne
    @JoinColumn(name = "association_id", nullable = false)
    private Association association;
}