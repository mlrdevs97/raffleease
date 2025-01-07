package com.raffleease.raffleease.Domains.Tickets.Repository.Impl;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus;
import com.raffleease.raffleease.Domains.Tickets.Repository.ICustomTicketsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomTicketsRepositoryImpl implements ICustomTicketsRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Ticket> edit(List<Ticket> tickets, TicketStatus status) {
        List<Long> ticketIds = tickets.stream().map(Ticket::getId).toList();

        String updateQuery = "UPDATE Ticket t " +
                "SET t.status = :status " +
                "WHERE t.id IN :ticketIds";

        entityManager.createQuery(updateQuery)
                .setParameter("status", status)
                .setParameter("ticketIds", ticketIds)
                .executeUpdate();

        String selectQuery = "SELECT t FROM Ticket t " +
                "WHERE t.id IN :ticketIds";

        return entityManager.createQuery(selectQuery, Ticket.class)
                .setParameter("ticketIds", ticketIds)
                .getResultList();
    }

    @Override
    public List<Ticket> findByTicketNumber(Raffle raffle, TicketStatus status, String ticketNumber) {
        String queryString = "SELECT t FROM Ticket t " +
                "WHERE t.raffle = :raffle " +
                "AND t.status = :status " +
                "AND t.ticketNumber LIKE :ticketNumber " +
                "ORDER BY CAST(t.ticketNumber AS long)";

        return entityManager.createQuery(queryString, Ticket.class)
                .setParameter("raffle", raffle)
                .setParameter("status", TicketStatus.AVAILABLE)
                .setParameter("ticketNumber", "%" + ticketNumber + "%")
                .getResultList();
    }
}
