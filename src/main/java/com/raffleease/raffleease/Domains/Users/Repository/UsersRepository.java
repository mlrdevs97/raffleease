package com.raffleease.raffleease.Domains.Users.Repository;

import com.raffleease.raffleease.Domains.Users.Model.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, Long> {
    @Query("""
        SELECT u FROM User u 
        WHERE u.email = :identifier 
           OR u.phoneNumber = :identifier 
           OR u.userName = :identifier
    """)
    Optional<User> findByIdentifier(@Param("identifier") String identifier);
}
