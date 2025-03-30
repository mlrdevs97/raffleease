package com.raffleease.raffleease.Helpers;

import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketsCreate;

import java.time.LocalDateTime;
import java.util.List;

public class RaffleCreateBuilder {
    private String title = "Sample Raffle Title";
    private String description = "This is a sample raffle description.";
    private LocalDateTime endDate = LocalDateTime.now().plusDays(10);
    private List<ImageDTO> images = List.of();
    private TicketsCreate ticketsInfo = new TicketsCreateBuilder().build();

    public RaffleCreateBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public RaffleCreateBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public RaffleCreateBuilder withEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    public RaffleCreateBuilder withImages(List<ImageDTO> images) {
        this.images = images;
        return this;
    }

    public RaffleCreateBuilder withTicketsInfo(TicketsCreate ticketsInfo) {
        this.ticketsInfo = ticketsInfo;
        return this;
    }

    public RaffleCreate build() {
        return new RaffleCreate(title, description, endDate, images, ticketsInfo);
    }
}

