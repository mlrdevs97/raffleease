package com.raffleease.raffleease.Domains.Associations.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mapping.Association;
import org.springframework.stereotype.Repository;

@Repository
public interface IAssociationsRepository extends JpaRepository<Association, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByName(String name);
}
