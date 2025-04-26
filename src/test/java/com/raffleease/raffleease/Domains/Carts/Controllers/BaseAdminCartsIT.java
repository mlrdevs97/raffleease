package com.raffleease.raffleease.Domains.Carts.Controllers;

import com.raffleease.raffleease.Base.BaseIT;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

public class BaseAdminCartsIT extends BaseIT {
    protected Long raffleId;
    protected Raffle raffle;
    protected List<Ticket> tickets;
    protected Long ticketId;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        raffleId = createRaffle(associationId, accessToken);
        raffle = rafflesRepository.findById(raffleId).orElseThrow();
        tickets = ticketsRepository.findAllByRaffle(raffle);
        ticketId = tickets.get(0).getId();
    }
}