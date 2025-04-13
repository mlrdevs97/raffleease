package com.raffleease.raffleease.Domains.Associations.Repository;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationMembership;
import com.raffleease.raffleease.Domains.Users.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssociationsMembershipsRepository extends JpaRepository<AssociationMembership, Long> {
    boolean existsByAssociationAndUser(Association association, User user);
}
