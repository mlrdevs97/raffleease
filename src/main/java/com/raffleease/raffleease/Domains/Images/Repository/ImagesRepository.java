package com.raffleease.raffleease.Domains.Images.Repository;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository("ImagesRepository")
public interface ImagesRepository extends JpaRepository<Image, Long> {
    @Query("SELECT COUNT(i) FROM Image i WHERE i.raffle IS NULL AND i.association = :association")
    int countPendingImagesByAssociation(@Param("association") Association association);

    List<Image> findAllByRaffleIsNullAndCreatedAtBefore(LocalDateTime cutoff);
    List<Image> findAllByRaffleIsNullAndAssociation(Association association);
    List<Image> findAllByRaffleIsNullAndAssociationAndImageOrderGreaterThan(Association association, int deletedImageOrder);
    List<Image> findAllByRaffle(Raffle raffle);
    
    @Query("SELECT i.filePath FROM Image i WHERE i.filePath IS NOT NULL")
    List<String> findAllFilePaths();
}
