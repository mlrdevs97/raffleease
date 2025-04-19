package com.raffleease.raffleease.Domains.Tickets.Repository.Impl;

import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus;
import com.raffleease.raffleease.Domains.Tickets.Repository.CustomTicketsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomTicketsRepositoryImpl implements CustomTicketsRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Ticket> updateStatus(List<Ticket> tickets, TicketStatus status) {
        List<Long> ticketIds = tickets.stream().map(Ticket::getId).toList();

        String updateQuery = "UPDATE Ticket t " +
                "SET t.status = :status " +
                "WHERE t.id IN :ticketIds";

        entityManager.createQuery(updateQuery)
                .setParameter("status", status)
                .setParameter("ticketIds", ticketIds)
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        String selectQuery = "SELECT t FROM Ticket t " +
                "WHERE t.id IN :ticketIds";

        return entityManager.createQuery(selectQuery, Ticket.class)
                .setParameter("ticketIds", ticketIds)
                .getResultList();
    }

    @Override
    public List<Ticket> search(Raffle raffle, String ticketNumber, TicketStatus status, Customer customer) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Ticket> query = cb.createQuery(Ticket.class);
        Root<Ticket> ticket = query.from(Ticket.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(ticket.get("raffle"), raffle));

        if (status != null) {
            predicates.add(cb.equal(ticket.get("status"), status));
        }
        if (ticketNumber != null) {
            predicates.add(cb.equal(ticket.get("ticketNumber"), ticketNumber));
        }
        if (customer != null) {
            predicates.add(cb.equal(ticket.get("customer"), customer));
        }

        query.select(ticket).where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(query).getResultList();
    }
}











