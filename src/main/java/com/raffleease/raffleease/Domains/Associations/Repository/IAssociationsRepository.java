package com.raffleease.raffleease.Domains.Associations.Repository;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface IAssociationsRepository extends JpaRepository<Association, Long> {
    Optional<Association> findByIdentifier(String identifier);
}
