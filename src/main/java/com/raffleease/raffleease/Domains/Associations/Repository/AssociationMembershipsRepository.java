package com.raffleease.raffleease.Domains.Associations.Repository;

import com.raffleease.raffleease.Domains.Associations.Model.AssociationMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssociationMembershipsRepository extends JpaRepository<AssociationMembership, Long> {
}
