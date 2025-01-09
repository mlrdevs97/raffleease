package com.raffleease.raffleease.Domains.Users.Repository.Impl;

import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Repository.ICustomRepository;
import com.raffleease.raffleease.Domains.Users.Services.Impls.UserDetailsServiceImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CustomRepositoryImpl implements ICustomRepository {
    private final EntityManager entityManager;
    private final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Override
    public Optional<User> findByIdentifier(String identifier) {
        String query = "SELECT u FROM User u " +
                "WHERE u.email = :identifier " +
                "OR u.phoneNumber = :identifier " +
                "OR u.username = :identifier";

        TypedQuery<User> typedQuery = entityManager.createQuery(query, User.class)
                .setParameter("identifier", identifier)
                .setMaxResults(1);

        try {
            User user = typedQuery.getSingleResult();
            return Optional.of(user);
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }
}
