package com.raffleease.raffleease.Domains.Associations.Repository;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAssociationsRepository extends JpaRepository<Association, Long> {
}
