package com.raffleease.raffleease.Domains.Raffles.Model;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus.PENDING;
import static jakarta.persistence.CascadeType.ALL;
import static java.math.BigDecimal.ZERO;

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

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime completedAt;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal ticketPrice;

    @Column(nullable = false)
    private Long firstTicketNumber;

    @Column(nullable = false)
    private Long totalTickets;

    @Column(nullable = false)
    private Long availableTickets;

    @Column(nullable = false)
    private Long soldTickets;

    @Column(nullable = false)
    private BigDecimal revenue;

    @OneToMany(mappedBy = "raffle", cascade = ALL, orphanRemoval = true)
    private List<Image> images;

    @OneToMany(mappedBy = "raffle", cascade = ALL, orphanRemoval = true)
    private List<Ticket> tickets;

    @ManyToOne
    @JoinColumn(name = "association_id", nullable = false)
    private Association association;

    private CompletionReason completionReason;

    @OneToOne
    @JoinColumn(name = "winning_ticket_id")
    private Ticket winningTicket;
}