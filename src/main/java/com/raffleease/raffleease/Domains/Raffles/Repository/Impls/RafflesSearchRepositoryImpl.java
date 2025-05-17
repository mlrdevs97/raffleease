package com.raffleease.raffleease.Domains.Raffles.Repository.Impls;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleSearchFilters;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Repository.RafflesSearchRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class RafflesSearchRepositoryImpl implements RafflesSearchRepository {
    private final EntityManager entityManager;

    @Override
    public Page<Raffle> search(RaffleSearchFilters searchFilters, Long associationId, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Raffle> query = cb.createQuery(Raffle.class);
        Root<Raffle> raffle = query.from(Raffle.class);
        Join<Raffle, Association> associationJoin = raffle.join("association");
        List<Predicate> predicates = buildPredicates(searchFilters, associationId, cb, raffle, associationJoin);
        query.select(raffle).distinct(true).where(predicates.toArray(new Predicate[0]));

        String sortBy = searchFilters.sortBy();
        String sortDirection = searchFilters.sortDirection();
        Path<?> sortPath;
        if ("title".equalsIgnoreCase(sortBy)) {
            sortPath = raffle.get("title");
        } else if ("startDate".equalsIgnoreCase(sortBy)) {
            sortPath = raffle.get("startDate");
        } else if ("endDate".equalsIgnoreCase(sortBy)) {
            sortPath = raffle.get("endDate");
        } else {
            sortPath = raffle.get("createdAt");
        }
        if ("asc".equalsIgnoreCase(sortDirection)) {
            query.orderBy(cb.asc(sortPath));
        } else {
            query.orderBy(cb.desc(sortPath));
        }

        List<Raffle> resultList = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Raffle> countRaffle = countQuery.from(Raffle.class);
        Join<Raffle, Association> countAssociationJoin = countRaffle.join("association");
        List<Predicate> countPredicates = buildPredicates(searchFilters, associationId, cb, countRaffle, countAssociationJoin);
        countQuery.select(cb.countDistinct(countRaffle)).where(cb.and(countPredicates.toArray(new Predicate[0])));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }

    private List<Predicate> buildPredicates(
            RaffleSearchFilters searchFilters,
            Long associationId,
            CriteriaBuilder cb,
            Root<Raffle> raffle,
            Join<Raffle, Association> associationJoin
    ) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(associationJoin.get("id"), associationId));
        if (searchFilters.title() != null) {
            predicates.add(cb.like(cb.lower(raffle.get("title")), "%" + searchFilters.title().toLowerCase() + "%"));
        }
        if (searchFilters.status() != null) {
            predicates.add(cb.equal(raffle.get("status"), searchFilters.status()));
        }
        return predicates;
    }
} 