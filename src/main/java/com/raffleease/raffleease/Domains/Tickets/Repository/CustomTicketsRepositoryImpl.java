package com.raffleease.raffleease.Domains.Tickets.Repository;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomTicketsRepositoryImpl implements ICustomTicketsRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void updateStatusAndReservationFlag(LocalDateTime reservationTime) {
        String query = "UPDATE Ticket t " +
                "SET t.status = 'AVAILABLE', t.reservationFlag = NULL " +
                "WHERE t.status = 'RESERVED' AND t.reservationTime < :reservationTime";

        entityManager.createQuery(query)
                .setParameter("reservationTime", reservationTime)
                .executeUpdate();
    }

    @Override
    public void updateReservationTime(LocalDateTime reservationTime) {
        String query = "UPDATE Ticket t " +
                "SET t.reservationTime = NULL " +
                "WHERE t.reservationTime < :reservationTime";

        entityManager.createQuery(query)
                .setParameter("reservationTime", reservationTime)
                .executeUpdate();
    }

    @Override
    public void setRaffle(Raffle raffle, Set<Ticket> tickets) {
        String query = "UPDATE Ticket t " +
                "SET t.raffle = :raffle " +
                "WHERE t.id IN :ticketIds";

        entityManager.createQuery(query)
                .setParameter("raffle", raffle)
                .setParameter("ticketIds", tickets.stream()
                        .map(Ticket::getId)
                        .toList())
                .executeUpdate();
    }


    @Override
    public List<Object[]> findRafflesAndUpdatedTicketCount(LocalDateTime threshold) {
        String query = "SELECT t.raffle, COUNT(t) " +
                "FROM Ticket t " +
                "WHERE t.status = 'RESERVED' AND t.reservationTime < :threshold " +
                "GROUP BY t.raffle";

        return entityManager.createQuery(query, Object[].class)
                .setParameter("threshold", threshold)
                .getResultList();
    }
}
