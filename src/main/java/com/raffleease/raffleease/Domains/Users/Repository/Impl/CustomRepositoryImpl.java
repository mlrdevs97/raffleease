package com.raffleease.raffleease.Domains.Users.Repository.Impl;

import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Repository.ICustomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Repository
public class CustomRepositoryImpl implements ICustomRepository {
    private final EntityManager entityManager;

    @Value("${spring.application.config.is_test}")
    private boolean test;

    @Override
    public Optional<User> findByIdentifier(String identifier) {
        if (test) log.debug("Executing query for identifier: {}", identifier);
        String query = "SELECT u FROM User u WHERE u.email = :identifier OR u.phoneNumber = :identifier OR u.userName = :identifier";
        TypedQuery<User> typedQuery = entityManager.createQuery(query, User.class)
                .setParameter("identifier", identifier).setMaxResults(1);

        try {
            User user = typedQuery.getSingleResult();
            if (test) log.debug("Query successful, user found: {}", user.getId());
            return Optional.of(user);
        } catch (NoResultException ex) {
            if (test) log.warn("No user found for identifier: {}", identifier);
            return Optional.empty();
        }
    }
}
