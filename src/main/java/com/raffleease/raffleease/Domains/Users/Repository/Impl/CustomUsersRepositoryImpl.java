package com.raffleease.raffleease.Domains.Users.Repository.Impl;

import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Repository.CustomUsersRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CustomUsersRepositoryImpl implements CustomUsersRepository {
    private final EntityManager entityManager;

    @Override
    public Optional<User> findByIdentifier(String identifier) {
        String query = "SELECT u FROM User u WHERE u.email = :identifier OR u.phoneNumber = :identifier OR u.userName = :identifier";
        TypedQuery<User> typedQuery = entityManager.createQuery(query, User.class)
                .setParameter("identifier", identifier).setMaxResults(1);

        try {
            User user = typedQuery.getSingleResult();
            return Optional.of(user);
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }
}
