package com.raffleease.raffleease.Domains.Tickets.Repository;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITicketsRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByRaffleAndStatus(Raffle raffle, TicketStatus status);
}
