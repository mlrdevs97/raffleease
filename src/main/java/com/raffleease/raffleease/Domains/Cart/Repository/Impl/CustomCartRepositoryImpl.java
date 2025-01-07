package com.raffleease.raffleease.Domains.Cart.Repository.Impl;

import com.raffleease.raffleease.Domains.Cart.Model.Cart;
import com.raffleease.raffleease.Domains.Cart.Model.CartStatus;
import com.raffleease.raffleease.Domains.Cart.Repository.ICustomCartRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CustomCartRepositoryImpl implements ICustomCartRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void updateExpiredCart(LocalDateTime lastModified) {
        String query = "UPDATE Cart c " +
                "SET c.status = :expired, " +
                "c.lastModified = :currentTimestamp " +
                "WHERE c.lastModified < :lastModified " +
                "AND c.status = :active";

        entityManager.createQuery(query)
                .setParameter("lastModified", lastModified)
                .setParameter("currentTimestamp", LocalDateTime.now())
                .setParameter("expired", CartStatus.EXPIRED)
                .setParameter("active", CartStatus.ACTIVE)
                .executeUpdate();
    }


    @Override
    public List<Cart> findExpiredCarts(LocalDateTime lastModified) {
        String query = "SELECT c FROM Cart c " +
                "WHERE c.lastModified < :lastModified " +
                "AND c.status = :status";

        return entityManager.createQuery(query, Cart.class)
                .setParameter("lastModified", lastModified)
                .setParameter("status", CartStatus.ACTIVE)
                .getResultList();
    }
}
