package com.raffleease.raffleease.Helpers;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;

public class RaffleBuilder {
    private String title = "Test Raffle";
    private String description = "This is a test raffle.";
    private RaffleStatus status = RaffleStatus.PENDING;
    private String URL = "https://example.com/raffle";
    private LocalDateTime startDate = LocalDateTime.now().plusDays(1);
    private LocalDateTime endDate = LocalDateTime.now().plusDays(10);
    private BigDecimal ticketPrice = new BigDecimal("5.00");
    private Long firstTicketNumber = 1L;
    private BigDecimal revenue = ZERO;
    private Long totalTickets = 100L;
    private Long availableTickets = 100L;
    private Long soldTickets = 0L;
    private List<Image> images = new ArrayList<>();
    private List<Ticket> tickets = new ArrayList<>();
    private Association association;

    public RaffleBuilder(Association association) {
        this.association = association;
    }

    public RaffleBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public RaffleBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public RaffleBuilder withStatus(RaffleStatus status) {
        this.status = status;
        return this;
    }

    public RaffleBuilder withURL(String URL) {
        this.URL = URL;
        return this;
    }

    public RaffleBuilder withStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public RaffleBuilder withEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    public RaffleBuilder withTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
        return this;
    }

    public RaffleBuilder withFirstTicketNumber(Long firstTicketNumber) {
        this.firstTicketNumber = firstTicketNumber;
        return this;
    }

    public RaffleBuilder withRevenue(BigDecimal revenue) {
        this.revenue = revenue;
        return this;
    }

    public RaffleBuilder withTotalTickets(Long totalTickets) {
        this.totalTickets = totalTickets;
        return this;
    }

    public RaffleBuilder withAvailableTickets(Long availableTickets) {
        this.availableTickets = availableTickets;
        return this;
    }

    public RaffleBuilder withSoldTickets(Long soldTickets) {
        this.soldTickets = soldTickets;
        return this;
    }

    public RaffleBuilder withImages(List<Image> images) {
        this.images = images;
        return this;
    }

    public RaffleBuilder withTickets(List<Ticket> tickets) {
        this.tickets = tickets;
        return this;
    }

    public RaffleBuilder withAssociation(Association association) {
        this.association = association;
        return this;
    }

    public Raffle build() {
        Raffle raffle = new Raffle();
        raffle.setTitle(title);
        raffle.setDescription(description);
        raffle.setStatus(status);
        raffle.setURL(URL);
        raffle.setStartDate(startDate);
        raffle.setEndDate(endDate);
        raffle.setTicketPrice(ticketPrice);
        raffle.setFirstTicketNumber(firstTicketNumber);
        raffle.setRevenue(revenue);
        raffle.setTotalTickets(totalTickets);
        raffle.setAvailableTickets(availableTickets);
        raffle.setSoldTickets(soldTickets);
        raffle.setImages(images);
        raffle.setTickets(tickets);
        raffle.setAssociation(association);
        return raffle;
    }
}
