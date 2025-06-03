package com.raffleease.raffleease.Domains.Raffles.Repository;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RafflesRepository extends JpaRepository<Raffle, Long>, RafflesSearchRepository {
    List<Raffle> findByAssociation(Association association);

    @Query("""
    SELECT r FROM Raffle r
    JOIN r.statistics s
    WHERE r.status <> :status
      AND (
            s.availableTickets = 0 OR
            s.soldTickets = r.totalTickets OR
            r.endDate <= CURRENT_TIMESTAMP
          )
    """)
    List<Raffle> findAllEligibleForCompletion(@Param("status") RaffleStatus status);
    List<Raffle> findAllByAssociation(Association association);
}
