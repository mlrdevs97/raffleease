package com.raffleease.raffleease.Domains.Raffles.Repository;

import com.raffleease.raffleease.Domains.Raffles.Model.RaffleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagesRepository extends JpaRepository<RaffleImage, Long> {
}
